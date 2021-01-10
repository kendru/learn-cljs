(ns notes.ui.util
  (:require [notes.state :as state]
            [notes.routes :as routes]
            [notes.command :refer [dispatch!]]))

(defn link [text {:keys [route-params class]
                  :or {class ""}}]
  [:a {:href "#"
       :on-click #(do (.preventDefault %)
                      (dispatch! :route/navigate route-params))
       :class (str class
                   (if (routes/matches? route-params (:current-route @state/app))
                     " active" ""))}
   text])

(defn button [text {:keys [route-params]}]
  (link text {:class "button" :route-params route-params}))
