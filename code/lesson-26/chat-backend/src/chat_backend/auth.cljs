(ns chat-backend.auth)

(defn authenticate! [conn-state user]
  (swap! conn-state #(-> %
                         (assoc :authenticated? true)
                         (assoc :user user))))

(defn authenticated? [conn-state]
  (= true (:authenticated? @conn-state)))

(defn current-user [conn-state]
  (:user @conn-state))

(defn with-authentication [handler]
  (fn [{:keys [ws] :as deps} conn-state & rest]
    (if (authenticated? conn-state)
      (apply handler deps conn-state rest)
      (.send ws (pr-str [:error {:message "Authentication required"}])))))
