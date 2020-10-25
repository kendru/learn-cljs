(ns notes.ui.sidebar
  (:require [notes.state :as state]
            [notes.routes :as routes]
            [notes.command :refer [dispatch!]]))

(defn link [text & route-params]
  [:a {:href "#"
       :on-click #(do (.preventDefault %)
                      (dispatch! :route/navigate! route-params))
       :class (if (routes/matches? route-params (:current-route @state/app))
                "active" "")}
    text])

(defn home-nav []
  [:nav
    [:ul
      [:li [link "Start Session" :start-session]]
      [:li [link "Join Existing Session" :join-session {:session-id "123"}]]]])

(def sidebar-component-mapping
  {:start-session home-nav
   :join-session home-nav})

(defn sidebar []
  [:div.sidebar
    [:div.sidebar-content
      (if-let [component (sidebar-component-mapping (first (:current-route @state/app)))]
        [component]
        [home-nav])]
    [:footer.sidebar-footer "CLJS Notes"]])

; (match (:current-route @state/app
;         :start-session []
;         :join-session []))
