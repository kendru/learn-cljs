(ns chat-backend.db)

(defn rollback-fn [client done]
  (fn [err]
    (when err
      (js/console.error "Error in transaction" (.-stack err)))
    (.query client "ROLLBACK"
      (fn [err]
        (when err (js/console.log "Error rolling back transaction" (.-stack err)))
        (done)))))

(defn commit-fn [client done]
  (fn []
    (.query client "COMMIT"
      (fn [err]
        (when err (js/console.log "Error committing transaction" (.-stack err)))
        (done)))))

(defn connection [db-pool cb]
  (.connect db-pool
    (fn [err client done]
      (if (some? err)
        (cb err)
        (cb nil {:client client
                 :done done})))))

(defn transaction [db-pool cb]
  (connection db-pool
    (fn [err {:keys [client done]}]
      (if (some? err)
        (cb err)
        (.query client "BEGIN"
          (fn [err]
            (if err
              (cb err)
              (cb nil {:client client
                       :commit (commit-fn client done)
                       :rollback (rollback-fn client done)}))))))))

(defn result-row-cb [callback]
  (fn [err res]
    (if err
      (callback err)
      (callback nil (js->clj (aget res "rows" 0) :keywordize-keys true)))))

(defn result-rows-cb [callback]
  (fn [err res]
    (if err
      (callback err)
      (callback nil (js->clj (aget res "rows") :keywordize-keys true)))))