(ns notes.event-handlers.api-data
  (:require [notes.state :refer [register-handler!]]
            [notes.command :refer [dispatch!]]))

(defn map-values [f m]
  (into {} (for [[k v] m] [k (f v)])))

(defn make-index [coll & {:keys [index-fn value-fn group-fn]
                          :or {value-fn identity
                               group-fn identity}}]
  (->> coll
       (group-by index-fn)
       (map-values #(group-fn (mapv value-fn %)))))

(defn get-links [notes]
  (mapcat (fn [note]
            (for [tag (:tags note)]
              {:note-id (:id note)
               :tag-id (:id tag)}))
          notes))

(defn normalize-notes [notes]
  (let [links (get-links notes)
        notes-without-tags (mapv #(dissoc % :tags) notes)
        all-note-tags (mapcat :tags notes)]
    {:notes (make-index notes-without-tags
                        :index-fn :id
                        :group-fn first)
     :tags (make-index all-note-tags
                       :index-fn :id
                       :group-fn first)
     :notes-tags
     {:by-note-id
      (make-index links
                  :index-fn :note-id
                  :value-fn :tag-id)
      :by-tag-id
      (make-index links
                  :index-fn :tag-id
                  :value-fn :note-id)}}))

(defn update-normalized-notes [db notes]
  (let [{:keys [notes tags notes-tags]} (normalize-notes notes)]
    (update db :data #(-> %
                          (update :notes merge notes)
                          (update :tags merge tags)
                          (assoc :notes-tags notes-tags)))))

(register-handler!
 :notes/received
 (fn [db payload]
   (update-normalized-notes db payload)))

(register-handler!
 :note/received
 (fn [db payload]
   (update-normalized-notes db [payload])))

(register-handler!
 :note/created
 (fn [db payload]
   (let [{:keys [id title]} payload]
     (dispatch! :notification/add
                {:type :info
                 :text (str "Note created: " title)})
     (dispatch! :route/navigate [:edit-note {:note-id id}])
     (assoc-in db [:data :notes id]
               (dissoc payload :tags)))))

(register-handler!
 :note/updated
 (fn [db payload]
   (let [{:keys [title id]} payload]
     (dispatch! :notification/add
                {:type :info
                 :text (str "Note saved: " title)})
     (dispatch! :notes/get-note id)
     (assoc-in db [:data :notes id] payload))))

(register-handler!
 :note/tagged
 (fn [db payload]
   (let [{:keys [note-id tag-id]} payload]
     (-> db
         (update-in [:data :notes-tags :by-note-id note-id] conj tag-id)
         (update-in [:data :notes-tags :by-tag-id tag-id] conj note-id)))))

(register-handler!
 :tags/received
 (fn [db payload]
   (update-in db [:data :tags]
              merge (make-index payload
                                :index-fn :id
                                :group-fn first))))

(register-handler!
 :tag/created
 (fn [db payload]
   (assoc-in db [:data :tags (:id payload)] payload)))
