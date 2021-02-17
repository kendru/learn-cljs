(ns learn-cljs.notes.ui.main
  (:require [learn-cljs.notes.state :as state]
            [learn-cljs.notes.ui.views.home :refer [home]]
            [learn-cljs.notes.ui.views.note-form :refer [note-form]]))

(defn not-found []
  [:section.hero
   [:h1.title "Page Not Found!"]])

(defn main []
  (let [[route params query] (:current-route @state/app)]
    [:div.main
     (case route
       :home [home]
       :create-note [note-form]
       :edit-note [note-form]
       [not-found])]))
