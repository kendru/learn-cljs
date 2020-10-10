(ns estimates.command
  (:require [estimates.api :as api]
            [estimates.messages :as msg]
            [cljs.core.async :refer [go-loop pub sub chan put! <!]]))

(defn handle-navigate! [route-params]
  (msg/emit! :route/navigated route-params))

(defn handle-session-start! [_]
  (let [id (js/uuidv4_TEMP)]
    (msg/emit! :session/pending)
    (api/send! :start-session {:name name})))


;; DIFF: channel not passed in - event-ch implicit through file.
(defn dispatch! [command payload]
 (case command
   ;; ... handle other commands
   :route/navigate! (handle-navigate! payload)
   :session/start! (handle-session-start! payload)))
