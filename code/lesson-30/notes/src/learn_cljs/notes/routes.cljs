(ns learn-cljs.notes.routes
  (:require [learn-cljs.notes.events :refer [emit!]]
            [bide.core :as bide]))

(defonce router
  (bide/router [["/" :home]
                ["/notes/new" :create-note]
                ["/notes/:note-id" :edit-note]]))

(defn get-url [route-params]
  (str "#"
       (apply bide/resolve router route-params)))

(defn matches? [route-params current-route]
  (= (get-url route-params)
     (get-url current-route)))

(defn navigate! [route-params]
  (apply bide/navigate! router route-params))

(defn- on-navigate [name params query]
  (emit! :route/navigated [name params query]))

(defn initialize! []
  (bide/start! router {:default :routes/home
                       :on-navigate on-navigate}))
