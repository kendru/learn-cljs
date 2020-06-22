(ns notifications.command-event
  (:require [reagent.core :as r]
            [cljs.core.async :refer [go-loop pub sub chan <! put!]]))

(defonce evt-ch (chan 1))
(defonce evt-bus (pub evt-ch ::type))

(defn emit!
 ([type] (emit! type nil))
 ([type payload]
  (put! evt-ch {::type type
                ::payload payload})))

;; ... Other handlers

(defn handle-user-form-submit! [form-data]
  (let [{:keys [first-name last-name]} form-data]
    ;; ... emit other events
    (emit! :notification/added (str "Welcome, " first-name " " last-name))))

(defn dispatch! [command payload]
 (case command
   ;; ... handle other commands
   :user-form/submit! (handle-user-form-submit! payload)))

(def initial-state
  {:messages []
   :next-id 0})

(defn add-notification [state id text]
  (-> state
      (update :messages conj {:id id
                              :text text})
      (assoc :next-id (inc id))))

(defn remove-notification [state id]
  (update state :messages
    (fn [messages]
      (filterv #(not= id (:id %)) messages))))

(defn listen-for-added! [state]
  (let [added (chan)]
    (sub evt-bus :notification/added added)
    (go-loop []
      (let [text (::payload (<! added))
            id (:next-id @state)]
        (swap! state add-notification id text)
        (js/setTimeout #(swap! state remove-notification id) 10000)
        (recur)))))

(defn notifications []
  (let [state (r/atom initial-state)]
    (listen-for-added! state)
    (fn []
      [:div.messages
        (for [msg (:messages @state)
              :let [{:keys [id text]} msg]]
          ^{:key id}
          [:div.notification.is-info
            [:button.delete {:on-click #(swap! state remove-notification id)}]
            [:div.body text]])])))

(defonce form-state (r/atom {:first-name ""
                             :last-name ""}))

(defn update-value [e field]
  (swap! form-state assoc field (.. e -target -value)))

(defn submit-input []
  (dispatch! :user-form/submit! @form-state)
  (swap! form-state assoc :first-name ""
                          :last-name ""))

(defn input-field [key label]
  [:div.field
    [:label.label label]
    [:div.control
      [:input.input {:value (get @form-state key)
                     :on-change #(update-value % key)}]]])

(defn input-form []
  [:div.form
    [input-field :first-name "First Name"]
    [input-field :last-name "Last Name"]
    [:div.field
      [:button.button {:on-click submit-input}
        "Add"]]])

(defn app []
  [:div.container
    [notifications]
    [input-form]])