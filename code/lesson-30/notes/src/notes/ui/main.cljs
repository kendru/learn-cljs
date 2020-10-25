(ns notes.ui.main
  (:require [notes.state :as state]
            [notes.ui.animation :as animation]
            [notes.ui.card :as card]
            [notes.command :refer [dispatch!]]
            [reagent.core :as r]))

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

(defn start-session []
  (let [name (r/atom "")]
    (fn []
      [:section.hero.is-fullheight
        [:div.hero-body
          [:div.container
            [:div.level
              [:div.level-item
                [animation/slide-in {:direction :left}
                  [card/card
                    [card/text-header "Name Your Session"]
                    [card/content
                      [:div.field
                        [:label.label {:for "session-name"} "Name"]
                        [:div.control
                          [:input.input {:id "session-name"
                                         :type "text"
                                         :value @name
                                         :on-change #(reset! name (.. % -target -value))}]]]]
                    [card/footer
                      [card/footer-item [:a.button.is-primary {:on-click #(dispatch! :session/start! @name)} "Start!"]]]]]]]]]])))

(defn join-session []
  [:div.section
    "Joining session"])

(defn main []
  (let [[route params query] (:current-route @state/app)]
    [:div.main
      (case route
        :start-session [start-session]
        :join-session [join-session]
        [not-found])]))



; (match (:current-route @state/app
;         :start-session []
;         :join-session []))
