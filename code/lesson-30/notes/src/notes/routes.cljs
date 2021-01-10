(ns notes.routes
  (:require [notes.events :refer [emit!]]
            [bide.core :as bide]))

(defonce router
  (bide/router [["/" :home]
                ["/notes/new" :create-note]
                ["/notes/:note-id" :edit-note]]))

(defn matches? [route-params current-route]
  (every? (fn [[fst snd]] (= fst snd))
          (map vector route-params current-route)))

(defn navigate! [route-params]
  (apply bide/navigate! router route-params))

(defn- on-navigate [name params query]
  (emit! :route/navigated [name params query]))

(defonce initialized?
  (do
    (bide/start! router {:default :routes/new-session
                         :on-navigate on-navigate})
    true))
