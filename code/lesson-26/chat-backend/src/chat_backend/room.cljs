(ns chat-backend.room
  (:require [chat-backend.db :as db]))

(defn list-rooms [db callback]
  (.query db
    "SELECT
      id, name
    FROM \"room\"
    ORDER BY created_at ASC
    LIMIT 1000"
    #js []
    (db/result-rows-cb callback)))

(defn create-room [db created-by name callback]
  (.query db
    "INSERT INTO \"room\" (name, created_by)
     VALUES ($1, $2)
     RETURNING id, name"
    #js [name created-by]
    (db/result-row-cb callback)))