(ns notes.messages
  (:require [cljs.core.async :refer [go-loop pub sub chan put! <!]]))

(def DEBUG true)

(defonce evt-ch (chan 1))
(defonce evt-bus (pub evt-ch ::type))

(defn emit!
 ([type] (emit! type nil))
 ([type payload]
  (let [evt {::type type
             ::payload payload}]
    (when DEBUG
      (println "Emit:" evt))
    (put! evt-ch evt))))

(defn listen [type callback]
  (let [ch (chan)]
    (sub evt-bus type ch)
    (go-loop []
      (callback (::payload (<! ch)))
      (recur))))

