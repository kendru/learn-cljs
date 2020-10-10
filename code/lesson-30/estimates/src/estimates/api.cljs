(ns estimates.api
  (:require [estimates.messages :refer [emit!]]
            [cljs.reader :refer [read-string]]))

(defonce api (atom nil))

(defn send!
 ([msg-type] (send! msg-type nil))
 ([msg-type payload]
  (.send @api
    (js/JSON.stringify
      (clj->js {:type msg-type
                :payload payload})))))

;; DIFF: msg-ch not passed in - using emit! directly
(defn init! [url]
  (let [ws (js/WebSocket. url)]
    (.addEventListener ws "message"
      (fn [msg]
        ;; DIFF: strs and JSON parsing
        (let [{:strs [type payload] :as e} (-> msg .-data js/JSON.parse js->clj)]
          (emit! (keyword (str "api/" type)) payload))))
    (reset! api ws)))

