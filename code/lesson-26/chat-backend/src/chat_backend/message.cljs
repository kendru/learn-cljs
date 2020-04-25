(ns chat-backend.message
  (:require [chat-backend.db :as db]))

(defn send-to-room! [db id sender content callback]
  (.query db
    "INSERT INTO room_message (room_id, sender, content)
      VALUES ($1, $2, $3)
     RETURNING sender, content, date_part('epoch', \"timestamp\")::int AS \"timestamp\""
    #js [id sender content]
    (db/result-row-cb callback)))

(defn send-to-user! [db recipient sender content callback]
  (.query db
    "INSERT INTO private_message (recipient, sender, content)
      VALUES ($1, $2, $3)
     RETURNING sender, content, date_part('epoch', \"timestamp\")::int AS \"timestamp\""
    #js [recipient sender content]
    (db/result-row-cb callback)))

(defn room-feed [db room-id callback]
  (.query db
    "WITH res AS (
        SELECT
            sender, content, date_part('epoch', \"timestamp\")::int AS \"timestamp\"
        FROM room_message
        WHERE room_id = $1
        ORDER BY \"timestamp\" DESC
        LIMIT 1000
     )
     SELECT * FROM res ORDER BY \"timestamp\" ASC"
    #js [room-id]
    (db/result-rows-cb callback)))

(defn conversation-feed [db username-1 username-2 callback]
  (.query db
    "WITH res AS (
        (
            SELECT
                sender, content, date_part('epoch', \"timestamp\")::int AS \"timestamp\"
            FROM private_message
            WHERE sender = $1 AND recipient = $2
            ORDER BY \"timestamp\" DESC
            LIMIT 500
        )
        UNION
        (
            SELECT
                sender, content, date_part('epoch', \"timestamp\")::int AS \"timestamp\"
            FROM private_message
            WHERE sender = $3 AND recipient = $4
            ORDER BY \"timestamp\" DESC
            LIMIT 500
        )
     )
     SELECT *
     FROM res
     ORDER BY \"timestamp\" ASC
     LIMIT 1000"
    #js [username-1 username-2 username-2 username-1]
    (db/result-rows-cb callback)))
