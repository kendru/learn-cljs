(ns learn-cljs.notes.api
  (:require [learn-cljs.notes.events :refer [emit!]]
            [learn-cljs.notes.errors :as err]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]))

(defn do-request!
  ([method path cb] (do-request! method path nil cb))
  ([method path body cb]
   (let [serialized-body (when body
                           (->> body
                                (cske/transform-keys csk/->camelCaseString)
                                (clj->js)
                                (js/JSON.stringify)))]
     (-> (js/fetch (str js/API_URL path)
                   (cond-> {:method (name method)
                            :headers {"Authorization" (str "Bearer " js/API_TOKEN)}
                            :credentials "include"}
                     (some? body)
                     (->
                      (assoc :body serialized-body)
                      (update :headers merge {"content-type" "application/json"}))

                     :always
                     clj->js))
         (.then (fn [res]
                  (if (.-ok res)
                    (when (= 200 (.-status res))
                      (.json res))
                    (throw (ex-info "API Request Failed"
                                    {:status-code (.-status res)
                                     :status (.-statusText res)}
                                    :api-failure)))))
         (.then #(->> %
                      (js->clj)
                      (cske/transform-keys csk/->kebab-case-keyword)
                      (err/ok)
                      (cb)))
         (.catch #(cb (err/error %)))))))

(defn- display-error [err]
  (emit! :notification/added
         {:type :error
          :text (str "API Error: " (ex-message err))}))

(defn- with-error-handling [f]
  (fn [res]
    (->> res
         (err/map f)
         (err/unwrap-or display-error))))

(defn get-notes! []
  (do-request! :get "/notes"
               (with-error-handling #(emit! :notes/received %))))

(defn get-note! [id]
  (do-request! :get (str "/notes/" id)
               (with-error-handling #(emit! :note/received %))))

(defn create-note! [note]
  (do-request! :post "/notes" note
               (with-error-handling #(emit! :note/created %))))

(defn update-note! [note]
  (do-request! :put (str "/notes/" (:id note)) note
               (with-error-handling #(emit! :note/updated note))))

(defn get-tags! []
  (do-request! :get "/tags"
               (with-error-handling #(emit! :tags/received %))))

(defn create-tag! [tag-name]
  (do-request! :post "/tags" {:name tag-name}
               (with-error-handling #(emit! :tag/created %))))

(defn tag-note! [note-id tag-id]
  (do-request! :put (str "/notes/" note-id "/tags/" tag-id)
               (with-error-handling
                 #(emit! :note/tagged {:note-id note-id
                                       :tag-id tag-id}))))

(defn do-search! [text]
  (println "TODO: Submit search" text))
