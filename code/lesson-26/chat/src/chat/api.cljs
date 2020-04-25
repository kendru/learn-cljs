(ns chat.api
  (:require [chat.command :as cmd]
            [cljs.reader :refer [read-string]]))

(defonce api (atom nil))

(defn ws-protocol []
  (if (= "http:" js/location.protocol) "ws:" "wss:"))

;; TODO: Make host/port configurable
(defn init! [cmd-ch]
  (let [ws (js/WebSocket. (str (ws-protocol) "//" js/location.hostname ":8080"))]
    (doto ws
      (.addEventListener "open"
        #(swap! api assoc :ready? true))

      (.addEventListener "close"
        #(swap! api assoc :ready? false))

      (.addEventListener "message"
        #(let [[type payload] (read-string (.-data %))]
           (cmd/dispatch! cmd-ch (keyword (str "api/" (name type))) payload)))

      (.addEventListener "error"
        #(swap! api assoc :ready? false)))

    (reset! api {:ws ws
                 :ready? false})))

(defn send!
 ([msg-type] (send! msg-type nil))
 ([msg-type payload]
  (.send (:ws @api)
    (pr-str (if (some? payload) [msg-type payload] [msg-type])))))
