(ns chat-backend.user
  (:require ["crypto" :as crypto]
            [chat-backend.db :as db]))

(def digest-iterations 100000)

(defn gen-digest [password]
  (let [salt (crypto/randomBytes 16)
        digest (crypto/pbkdf2Sync password salt digest-iterations 512 "sha512")]
    (str (.toString salt "hex")
         (.toString digest "hex"))))

(defn password-valid? [digest password]
  (let [salt-hex (js/Buffer.from (.substring digest 0 32) "hex")
        digest-hex (js/Buffer.from (.substring digest 32) "hex")
        attempted-digest (crypto/pbkdf2Sync password salt-hex digest-iterations 512 "sha512")]
    (.equals attempted-digest digest-hex)))

(defn display-attrs [user]
  (select-keys user [:username :first-name :last-name]))

(defn create-user [db {:keys [username password first-name last-name]} callback]
  (let [digest (gen-digest password)])
  (.query db
      "INSERT INTO \"user\" (username, password_digest, first_name, last_name) VALUES ($1, $2, $3, $4)"
      #js [username (gen-digest password) first-name last-name]
      callback))

(defn get-user [db username callback]
  (.query db
    "SELECT
        username,
        password_digest AS \"password-digest\",
        first_name AS \"first-name\",
        last_name AS \"last-name\"
     FROM \"user\"
     WHERE \"username\" = $1 AND username <> 'system'
     LIMIT 1"
     #js [username]
     (db/result-row-cb callback)))

(defn list-users [db callback]
  (.query db
    "SELECT
      username,
      first_name AS \"first-name\",
      last_name AS \"last-name\",
      COALESCE(user_activity.ts >= (NOW() - interval '10 minutes'), false) AS \"is-online?\"
    FROM \"user\"
    LEFT OUTER JOIN \"user_activity\" USING (username)
    WHERE username <> 'system'
    ORDER BY first_name ASC, last_name ASC
    LIMIT 1000"
    #js []
    (db/result-rows-cb callback)))

(defn delete-user [db username callback]
  (.query db
    "DELETE FROM \"user\" WHERE username = $1 AND username <> 'system'",
    #js [username]
    callback))

(defn bump-activity [db username callback]
  (.query db
    "INSERT INTO user_activity (username, ts)
      VALUES ($1, NOW())
    ON CONFLICT (username)
    DO UPDATE SET
      ts = EXCLUDED.ts"
    #js [username]
    callback))
