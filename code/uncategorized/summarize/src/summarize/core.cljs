(ns summarize.core
  (:require [goog.functions :refer [debounce]]
            [goog.dom :as gdom]
            [goog.events :as gevents]))

(enable-console-print!)

(defonce events (atom []))
(def result (gdom/getElement "result"))

(defn now []
  (.floor js/Math
    (/ (.getTime (js/Date.)) 1000)))

(defn longest-idle-time [events]
  (let [initial-acc {:max-idle 0
                     :last-ts (:timestamp (first events))}]
    (:max-idle
     (reduce (fn [{:keys [max-idle last-ts]} event]
               (let [ts (:timestamp event)
                     idle-time (- ts last-ts)]
                 {:max-idle (max max-idle idle-time)
                  :last-ts ts}))
             initial-acc
             events))))

(defn append-event! [event-name]
  (swap! events conj {:event event-name :timestamp (now)})
  (gdom/setTextContent result (longest-idle-time @events)))

(defn track-events! []
  (gevents/listen js/document "click" #(append-event! :click))
  (gevents/listen js/document "scroll" (debounce #(append-event! :scroll) 500))
  (gevents/listen js/document "keyup" (debounce #(append-event! :typing) 1000)))

(aset js/window "onload" track-events!)
