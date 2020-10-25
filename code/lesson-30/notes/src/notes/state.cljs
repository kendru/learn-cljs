(ns notes.state
  (:require [reagent.core :as r]))

(def initial-state
  {:current-route :start-session})

(defonce app (r/atom initial-state))

(defn set-route! [route]
  (swap! app assoc :current-route route))

