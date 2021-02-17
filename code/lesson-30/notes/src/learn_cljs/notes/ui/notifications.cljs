(ns learn-cljs.notes.ui.notifications
  (:require [reagent.core :as r]
            [learn-cljs.notes.ui.animation :as animation]
            [learn-cljs.notes.state :refer [app]]
            [learn-cljs.notes.command :refer [dispatch!]]))

(defn notifications []
  (let [messages (r/cursor app [:notifications :messages])]
    (fn []
      [:div.messages
       (for [msg @messages
             :let [{:keys [id type text]} msg]]
         ^{:key id}
         [animation/slide-in {:direction :top}
          [:div {:class (str "notification is-" (name type))}
           [:button.delete {:on-click #(dispatch! :notification/remove id)}]
           [:div.body text]]])])))
