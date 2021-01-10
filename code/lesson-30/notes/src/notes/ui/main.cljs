(ns notes.ui.main
  (:require [notes.state :as state]
            [notes.ui.animation :as animation]
            [notes.ui.card :as card]
            [notes.command :refer [dispatch!]]
            [reagent.core :as r]
            [notes.ui.views.home :refer [home]]))

(defn tile-single-test [t]
  (let [loaded? (r/atom false)]
    (js/setTimeout #(reset! loaded? true) 500)
    (fn [t]
      [:div.tile.is-4.is-parent
       [animation/duration 1000
        [animation/slide-in {:direction :right}
         [:div.card
          [:header.card-header
           [:p.card-header-title "Test Card"]]
          [:div.card-content
           [:div.content t]]]]]])))

(defn tile-test []
  [:div.tile.is-ancestor
   [tile-single-test "Now is the time for all good men to come to the aid of their party"]
   [tile-single-test "Now is the time for all good men to come to the aid of their party"]
   [tile-single-test "Now is the time for all good men to come to the aid of their party Now is the time for all good men to come to the aid of their partyNow is the time for all good men to come to the aid of their party Now is the time for all good men to come to the aid of their party"]])

(defn not-found []
  [:section.hero.is-fullheight
   [:div.hero-body
    [:div.container
     [:h1.title "Page Not Found!"]]]])

;; (defn home []
;;   (let [name (r/atom "")]
;;     (fn []
;;       [:div
;;        [:a.button.is-primary {:on-click #(dispatch! :notification/add {:text "Hello World"})} "Notify Me"]])))

(defn create-note []
  [:div.section
   "Joining session"])

(defn main []
  (let [[route params query] (:current-route @state/app)]
    [:div.main
     (case route
       :home [home]
       :create-note [create-note]
       [not-found])]))
