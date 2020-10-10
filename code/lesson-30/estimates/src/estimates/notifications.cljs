(ns estimates.components.notifications
  (:require [reagent.core :as r]
            [cljs.core.async :refer [go-loop sub chan <!]]
            [estimates.messages :as msg]))

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
    (msg/listen :notification/added added)
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
