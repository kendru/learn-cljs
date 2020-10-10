(ns estimates.routes
  (:require [estimates.state :as state]
            [estimates.messages :as msg]
            [bide.core :as bide]))

(defonce router
  (bide/router [["/sessions/new" :start-session]
                ["/sessions/:session-id" :join-session]]))

(defn matches? [route-params current-route]
  (every? (fn [[fst snd]] (= fst snd))
    (map vector route-params current-route)))

(defn on-navigate [name params query]
  (state/set-route! [name params query]))

(defonce initialized?
  (do
    (bide/start! router {:default :routes/new-session
                         :on-navigate on-navigate})
    (msg/listen :route/navigated
      (fn [route-params]
        (apply bide/navigate! router route-params)))
    true))
