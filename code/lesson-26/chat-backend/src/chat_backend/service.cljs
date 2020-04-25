(ns chat-backend.service
  (:require [chat-backend.user :as u]
            [chat-backend.room :as r]
            [chat-backend.auth :as auth]
            [chat-backend.db :as db]
            [cljs.pprint :refer [pprint]]
            [chat-backend.message :as m]
            [cljs.core.async :refer [go go-loop <!]]
            [cljs.reader :refer [read-string]]
            [chat-backend.util :refer [<fn <on <all]]))

(def connections (atom []))

(defn track-connection! [conn-state]
  (swap! connections conj conn-state))

(defn untrack-connection! [conn-state]
  (let [id (:id @conn-state)]
    (swap! connections
      (fn [conns]
        (remove #(->> % deref :id (= id)) conns)))))

(def conn-ids (atom 0))

(defn new-conn-state [ws]
  {:id (swap! conn-ids inc)
   :ws ws
   :authenticated? false
   :user nil
   :current-view {:type :none}})

(defn reply [ws msg]
  (.send ws (pr-str msg)))

(defn error-res [message]
  [:error {:message message}])

(defn not-implemented [ws type]
  (reply ws (error-res (str "Message type not implemented: " (name type)))))

(defn broadcast! [send-ws msg]
  (doseq [conn @connections]
    (when-not (= send-ws (:ws @conn))
      (reply (:ws @conn) msg))))

(defn broadcast-room-message! [room-id msg]
  (doseq [conn @connections]
    (let [{:keys [type id]} (:current-view @conn)]
      (when (and (= type :room)
               (= id room-id))
        (reply (:ws @conn) [:message-received {:message msg
                                               :room room-id}])))))

(defn deliver-private-message! [recipient {:keys [sender] :as msg}]
  (doseq [conn @connections]
    (let [{:keys [username]} (:user @conn)]
      (when (or (= sender username)
                (= recipient username))
        (reply (:ws @conn) [:message-received {:message msg
                                               :username (if (= sender username)
                                                           recipient
                                                           sender)}])))))

(defn sign-up [{:keys [ws db]} conn-state user-info]
  (if (every? user-info [:username :password :first-name :last-name])
    (go
      (<all [{:keys [client commit rollback]} (<fn db/transaction db)
             _ (<fn u/create-user client user-info)
             user (<fn u/get-user client (:username user-info))]
        (commit)
        (auth/authenticate! conn-state (u/display-attrs user))
        (reply ws [:authenticated (u/display-attrs user)])
        (broadcast! ws [:person-joined (u/display-attrs user)])
        (on-error e
          (println "Error signing up" e)
          (rollback e)
          (reply ws (error-res "Could not process sign up request")))))
    (reply ws (error-res "Missing required parameter"))))


(defn sign-in [{:keys [ws db]} conn-state creds]
  (if (every? creds [:username :password])
    (go
      (<all [user (<fn u/get-user db (:username creds))]
        (if (and (some? user)
                 (u/password-valid? (:password-digest user) (:password creds)))
          (do
            (auth/authenticate! conn-state (u/display-attrs user))
            (<! (<fn u/bump-activity db (:username user)))
            (reply ws [:authenticated (u/display-attrs user)])
            (broadcast! ws [:person-joined (u/display-attrs user)]))
          (reply ws [:authentication-error "Invalid password"]))
        (on-error e
          (println "Error signing in" e)
          (reply ws (error-res "Could not process sign up request")))))
    (reply ws (error-res "Missing required parameter"))))

(defn list-people [{:keys [ws db]} _]
  (go (<all [people (<fn u/list-users db)]
        (reply ws [:people-listed people])
        (on-error e
          (println "Error listing people" e)
          (reply ws (error-res "Could not list people"))))))

(defn set-view [{:keys [ws db]} conn-state view]
  (let [{:keys [type id username]} view]
    (if (or
          (and (= :room type) (some? id))
          (and (= :conversation type) (some? username)))
      (do
        (swap! conn-state assoc :current-view view)
        (reply ws [:view-set view])
        (case type
          :room (go (<all [messages (<fn m/room-feed db id)]
                      (reply ws [:messages-received {:messages messages
                                                     :room id}])
                      (on-error e
                        (println "Error getting messages" e)
                        (reply ws (error-res "Could not get message feed")))))
          :conversation (go (<all [messages (<fn m/conversation-feed db username (-> conn-state auth/current-user :username))]
                              (reply ws [:messages-received {:messages messages
                                                             :username username}])
                              (on-error e
                                (println "Error getting messages" e)
                                (reply ws (error-res "Could not get conversation feed")))))))
      (reply ws (error-res "Invalid view")))))

(defn add-message [{:keys [ws db]} conn-state message]
  (if-let [content (:content message)]
    (let [{:keys [type id username]} (:current-view @conn-state)
          sender (:username (auth/current-user conn-state))]
      (case type
        :room (go (<all [msg (<fn m/send-to-room! db id sender content)]
                    (broadcast-room-message! id msg)
                    (on-error e
                      (println "Error delivering message to room" e)
                      (reply ws (error-res "Could not send message to room")))))
        :conversation (go (<all [msg (<fn m/send-to-user! db username sender content)]
                            (deliver-private-message! username msg)
                            (on-error e
                              (println "Error delivering message to user" e)
                              (reply ws (error-res "Could not send message to user")))))
        (reply ws (error-res "View must be set"))))
    (reply ws (error-res "Missing required parameter"))))

(defn list-rooms [{:keys [ws db]} _]
  (go (<all [rooms (<fn r/list-rooms db)]
        (reply ws [:rooms-listed rooms])
        (on-error e
          (println "Error listing rooms" e)
          (reply ws (error-res "Could not list rooms"))))))

(defn create-room [{:keys [ws db]} conn-state room]
  (if-let [name (:name room)]
    (go
      (<all [room (<fn r/create-room db (:username (auth/current-user conn-state)) name)]
        (broadcast! nil [:room-created room])
        (on-error e
          (println "Error signing up" e)
          (reply ws (error-res "Could not create room")))))
    (reply ws (error-res "Missing required parameter"))))

(defn update-current-user-activity [{:keys [db]} conn-state]
  (let [user (auth/current-user conn-state)]
    (u/bump-activity db (:username user)
      (fn [err _]
        (when err (println "Error bumping user activity" e))))))

(defn handle-message [{:keys [ws] :as deps} conn-state msg]
  (when (auth/authenticated? conn-state)
    (update-current-user-activity deps conn-state))
  (try
    (let [[type payload] (read-string msg)]
      (case type
        :sign-up (sign-up deps conn-state payload)
        :sign-in (sign-in deps conn-state payload)
        :list-people ((auth/with-authentication list-people) deps conn-state)
        :list-rooms ((auth/with-authentication list-rooms) deps conn-state)
        :create-room ((auth/with-authentication create-room) deps conn-state payload)
        :set-view ((auth/with-authentication set-view) deps conn-state payload)
        :add-message ((auth/with-authentication add-message) deps conn-state payload)))
    (catch js/Object err
      (println "Error parsing request" (.-message err))
      (.send (:ws deps)
             (pr-str [:error {:message "Could not parse request"}])))))

(defn handle-connection [ws db-pool]
  (println "Received connection")
  (let [msg-chan (<on ws "message")
        deps {:db db-pool
              :ws ws}
        conn-state (atom (new-conn-state ws))]
    (track-connection! conn-state)

    (go-loop []
      (handle-message deps conn-state (first (<! msg-chan)))
      (recur))

    (go (<! (<on ws "close"))
      (println "Closed connection" @conn-state)
      (untrack-connection! conn-state)
      (when-let [username (get-in @conn-state [:user :username])]
        (broadcast! nil [:person-left username])))))

(defn attach [wss db-pool]
  (.on wss "connection" #(handle-connection % db-pool)))
