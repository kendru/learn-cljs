(ns notes.event-handlers.api-data
  (:require [notes.state :refer [register-handler!]]))

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
    {:notes {:by-id (make-index notes-without-tags
                                :index-fn :id
                                :group-fn first)}
     :tags {:by-id (make-index all-note-tags
                               :index-fn :id
                               :group-fn first)}
     :notes-tags
     {:by-note-id
      (make-index links
                  :index-fn :note-id
                  :value-fn :tag-id)
      :by-tag-id
      (make-index links
                  :index-fn :tag-id
                  :value-fn :note-id)}}))

(register-handler!
 :notes/received
 (fn [db payload]
   (let [{:keys [notes tags notes-tags]} (normalize-notes payload)]
     (update db :data #(-> %
                           (update :notes merge notes)
                           (update :tags merge tags)
                           (assoc :notes-tags notes-tags))))))

(register-handler!
 :note/created
 (fn [db payload]
   (println "TODO: Handle :note/created" payload)
   db))

(register-handler!
 :note/tagged
 (fn [db payload]
   (println "TODO: Handle :note/tagged" payload)
   db))

(register-handler!
 :tags/received
 (fn [db payload]
   (println "TODO: Handle :tags/received" payload)
   db))

(register-handler!
 :tag/created
 (fn [db payload]
   (println "TODO: Handle :tag/created" payload)
   db))
