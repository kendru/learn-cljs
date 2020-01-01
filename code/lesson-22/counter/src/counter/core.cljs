(ns counter.core
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime]
            [goog.dom :as gdom]
            [goog.events :as gevents]
            [clojure.string :as str]))

(enable-console-print!)

(defonce app-state (atom 0))

(def app-container (gdom/getElement "app"))

(defn render [state]
  (set! (.-innerHTML app-container)
        (hiccups/html
          [:div
            [:p "Counter: " [:strong state]]
            [:button {:id "up"} "+"]
            [:button {:id "down"} "-"]])))

(defonce is-initialized?
  (do
    (gevents/listen (gdom/getElement "app") "click"
      (fn [e]
        (condp = (aget e "target" "id")
          "up"   (swap! app-state inc)
          "down" (swap! app-state dec))))

    (add-watch app-state :counter-observer
      (fn [key atom old-val new-val]
        (render new-val)))

    (render @app-state)

    true))
