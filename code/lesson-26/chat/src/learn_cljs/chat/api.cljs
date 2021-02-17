(ns learn-cljs.chat.api
  (:require [learn-cljs.chat.message-bus :as bus]
            [cljs.reader :refer [read-string]]))

(defonce api (atom nil))

(defn send!
 ([msg-type] (send! msg-type nil))
 ([msg-type payload]
  (.send @api
    (pr-str (if (some? payload) [msg-type payload] [msg-type])))))

(defn init! [msg-ch url]
  (let [ws (js/WebSocket. url)]
    (.addEventListener ws "message"
      (fn [msg]
        (let [[type payload] (read-string (.-data msg))]
           (bus/dispatch! msg-ch (keyword (str "api/" (name type))) payload))))
    (reset! api ws)))

