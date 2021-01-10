(ns notes.api
  (:require [notes.events :refer [emit!]]
            [notes.errors :as err]
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
                     (merge {:headers {"content-type" "application/json"}
                             :body serialized-body})

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

(defn get-tags! []
  (do-request! :get "/tags"
               (fn [res]
                 (->> res
                      (err/map
                       #(emit! :tags/received %))
                      (err/unwrap-or display-error)))))

(defn create-tag! [tag-name]
  (do-request! :post "/tags" {:name tag-name}
               (fn [res]
                 (->> res
                      (err/map
                       #(emit! :tag/created %))
                      (err/unwrap-or display-error)))))

(defn get-notes! []
  (do-request! :get "/notes"
               (fn [res]
                 (->> res
                      (err/map
                       #(emit! :notes/received %))
                      (err/unwrap-or display-error)))))

(defn create-note! [note]
  (do-request! :post "/notes" note
               (fn [res]
                 (->> res
                      (err/map
                       #(emit! :note/created %))
                      (err/unwrap-or display-error)))))

(defn tag-note! [note-id tag-id]
  (do-request! :put (str "/notes/" note-id "/tags/" tag-id)
               (fn [res]
                 (->> res
                      (err/map
                       #(emit! :note/tagged {:note-id note-id
                                             :tag-id tag-id}))
                      (err/unwrap-or display-error)))))

(defn do-search! [text]
  (println "TODO: Submit search" text))

(comment
  (enable-console-print!)

  (let [items [{:id 1 :title "foo"}
               {:id 2 :title "bar"}]]
    (group-by :id items))

  (do
    (create-tag! "food")
    (create-tag! "list")
    (create-tag! "reading"))

  (do
    (create-note! {:title "Books to Read"
                   :content "These are the books that I have read: ..."})
    (create-note! {:title "Groceries"
                   :content "This is my grocery list: ..."}))

  (do
    (tag-note! 1 2)
    (tag-note! 1 3)
    (tag-note! 2 1)
    (tag-note! 2 2))

  (get-notes!)

  (defn get-links' [notes]
    (apply concat
           (for [note notes]
             (for [tag (:tags note)]
               {:note-id (:id note)
                :tag-id (:id tag)}))))


  (let [notes [{:id 1
                :title "Books to Read"
                :content "..."
                :tags [{:id 2 :name "list"}
                       {:id 3 :name "reading"}]}
               {:id 2
                :title "Groceries"
                :content "..."
                :tags [{:id 1 :name "food"}
                       {:id 2 :name "list"}]}]]
    (get-links'' notes))

  #_end)
;; DIFF: msg-ch not passed in - using emit! directly
