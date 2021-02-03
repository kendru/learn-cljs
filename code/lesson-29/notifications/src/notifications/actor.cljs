(ns notifications.actor
  (:require [reagent.core :as r]
            [cljs.core.async :refer [go-loop pub sub chan <! >! put! timeout]]))

(defn actor-system []
  (atom {}))

(defn send-to! [system to msg]
  (when-let [ch (get @system to)]
    (put! ch msg)))

(defn actor [system address init-state & {:as handlers}]
  (let [state (r/atom init-state)
        in-ch (chan)]
    (swap! system assoc address in-ch)
    (go-loop []
      (let [[type & payload] (<! in-ch)]
        (when-let [handler (get handlers type)]
          (apply handler state payload))
        (recur)))
    state))

;; Application

(defonce sys (actor-system))

(defn add-notification [state id text]
  (-> state
      (update :messages conj {:id id
                              :text text})
      (assoc :next-id (inc id))))

(defn remove-notification [state id]
  (update state :messages
    (fn [messages]
      (filterv #(not= id (:id %)) messages))))

(defonce notification-state
  (actor sys 'notifications
    {:messages []
     :next-id 0}

    :add-notification
    (fn [state text]
      (let [id (:next-id @state)]
        (swap! state add-notification id text)
        (js/setTimeout
          #(send-to! sys 'notifications
             [:remove-notification id])
          10000)))

    :remove-notification
    (fn [state id]
      (swap! state remove-notification id))))

(defonce form-state
  (actor sys 'input-form
    {:first-name ""
     :last-name ""}

    :update
    (fn [state field value]
      (swap! state assoc field value))

    :submit
    (fn [state]
      (let [{:keys [first-name last-name]} @state]
        (send-to! sys 'notifications
          [:add-notification (str "Welcome, " first-name " " last-name)]))
      (swap! state assoc
        :first-name ""
        :last-name ""))))

(defn notifications []
  [:div.messages
    (for [msg (:messages @notification-state)
          :let [{:keys [id text]} msg]]
      ^{:key id}
      [:div.notification.is-info
        [:button.delete {:on-click #(send-to! sys 'notifications
                                      [:remove-notification id])}]
        [:div.body text]])])

(defn input-field [key label]
  [:div.field
    [:label.label label]
    [:div.control
      [:input.input {:value (get @form-state key)
                     :on-change #(send-to! sys 'input-form
                                   [:update key (.. % -target -value)])}]]])

(defn input-form []
  [:div.form
    [input-field :first-name "First Name"]
    [input-field :last-name "Last Name"]
    [:div.field
      [:button.button {:on-click #(send-to! sys 'input-form
                                   [:submit])}
        "Add"]]])

(defn app []
  [:div.container
    [notifications]
    [input-form]])
