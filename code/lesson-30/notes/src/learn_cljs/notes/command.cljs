(ns learn-cljs.notes.command
  (:require [learn-cljs.notes.api :as api]
            [learn-cljs.notes.events :refer [emit!]]
            [learn-cljs.notes.routes :as routes]))

(defn handle-navigate! [route-params]
  (routes/navigate! route-params))

(defn handle-get-notes! [_]
  (api/get-notes!))

(defn handle-get-note! [id]
  (api/get-note! id))

(defn handle-create-note! [note]
  (api/create-note! note))

(defn handle-update-note! [note]
  (api/update-note! note))

(defn handle-get-tags! [_]
  (api/get-tags!))

(defn handle-create-tag! [tag-name]
  (api/create-tag! tag-name))

(defn handle-tag-note! [{:keys [note-id tag-id]}]
  (api/tag-note! note-id tag-id))

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
   (js/setTimeout
    (fn []
      (case command
        :route/navigate (handle-navigate! payload)

        :notes/get-notes (handle-get-notes! payload)
        :notes/get-note (handle-get-note! payload)
        :notes/create (handle-create-note! payload)
        :notes/update (handle-update-note! payload)
        :notes/tag (handle-tag-note! payload)

        :tags/get-tags (handle-get-tags! payload)
        :tags/create (handle-create-tag! payload)

        :notification/add (handle-add-notification! payload)
        :notification/remove (handle-remove-notification! payload)

        :search/update-input  (handle-update-search-input! payload)
        :search/submit-input  (handle-submit-search-input! payload)

        (js/console.error (str "Error: unhandled command: " command))))
    0)))
