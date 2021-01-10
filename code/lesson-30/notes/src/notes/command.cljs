(ns notes.command
  (:require [notes.api :as api]
            [notes.events :refer [emit!]]
            [notes.routes :as routes]))

(defn handle-navigate! [route-params]
  (routes/navigate! route-params))

(defn handle-get-notes! [_]
  (api/get-notes!))

(defn handle-add-notification! [notification]
  (emit! :notification/added notification))

(defn handle-remove-notification! [id]
  (emit! :notification/removed id))

(defn handle-update-search-input! [text]
  (emit! :search/input-updated text))

(defn handle-submit-search-input! [text]
  (api/do-search! text)
  (emit! :search/input-cleared))

(defn dispatch!
  ([command] (dispatch! command nil))
  ([command payload]
   (case command
     :route/navigate (handle-navigate! payload)

     :notes/get-notes (handle-get-notes! payload)

     :notification/add (handle-add-notification! payload)
     :notification/remove (handle-remove-notification! payload)

     :search/update-input  (handle-update-search-input! payload)
     :search/submit-input  (handle-submit-search-input! payload)

     (js/console.error (str "Error: unhandled command: " command)))))
