(ns fp.core
  (:require [clojure.string :as s]))

(enable-console-print!)

;; Composeable Functions

(def alan-p {:first-name "Alan"
             :last-name "Perlis"
             :online? false})

(def alan-t {:first-name "Alan"
             :last-name "Turing"
             :nickname "The Machine"
             :online? false})

(def robert-m {:first-name "Robert"
               :last-name "Morris"
               :online? true
               :hide? true})

(def users [alan-p alan-t robert-m])

(defn nickname [user]
  (or (:nickname user)
      (->> user
           ((juxt :first-name :last-name))
           (s/join " "))))

(defn bold [child]
  [:strong child])

(defn concat-strings [s1 s2]
  (s/trim (str s1 " " s2)))

(defn with-class [dom class-name]
  (if (map? (second dom))
    (update-in dom [1 :class] concat-strings class-name)
    (let [[tag & children] dom]
      (vec (concat [tag {:class class-name}]
                   children)))))

(defn with-status [dom entity]
  (with-class dom
    (if (:online? entity) "online" "offline")))

(defn user-status [user]
  [:div {:class "user-status"}
    ((juxt
      (comp bold nickname)
      (partial with-status [:span {:class "status-indicator"}]))
     user)])

;; Partial

(defn add [x y]
  (+ x y))

(def add-5 (partial add 5))

;; Referential Transparency

(defn get-time-of-day-greeting-impure []
  (condp >= (.getHours (js/Date.))
    11 "Good morning"
    15 "Good day"
    "Good evening"))

(defn get-current-hour []
  (.getHours (js/Date.)))

(defn get-time-of-day-greeting [hour]
  (condp >= hour
    11 "Good morning"
    15 "Good day"
    "Good evening"))

;; Immutable Data

(def blog {:title "Functional ClojureScript"
           :tags ["ClojureScript" "FP"]
           :rating 4})

(def new-blog
  (-> blog
      (update-in [:tags] conj "immutability")
      (update-in [:rating] inc)
      (update-in [:title] #(str % " for fun and profit"))
      (assoc :new? true)))

;; Closure

(defn make-adder [x]
  (fn [y]
    (add x y)))

(defn make-mailbox
 ([] (make-mailbox {:messages []
                    :next-id 1}))
 ([state]
  {:deliver!
   (fn [msg]
     (make-mailbox
       (-> state
          (update :messages conj
            (assoc msg :read? false
                       :id (:next-id state)))
          (update :next-id inc))))

   :next-unread
   (fn []
     (when-let [msg (->> (:messages state)
                         (filter (comp not :read?))
                         (first))]
       (dissoc msg :read?)))

   :read!
   (fn [id]
     (make-mailbox
       (update state :messages
         (fn [messages]
           (map #(if (= id (:id %)) (assoc % :read? true) %)
                 messages)))))}))

(defn call [obj method & args]
  (apply (get obj method) args))

(defn test-mailbox []
  (loop [mbox (-> (make-mailbox)
                  (call :deliver! {:subject "Objects are Cool"})
                  (call :deliver! {:subject "Closures Rule"}))]
    (when-let [next-message (call mbox :next-unread)]
      (println "Got message" next-message)
      (recur
        (call mbox :read! (:id next-message)))))
  (println "Read all messages!"))

;; Middleware

(defn handler [req]
  (println "Calling API with" req)
  {:response "is fake"})

(defn validate-request [req]
  (cond
    (nil? (:id req)) {:error "id must be present"}
    (nil? (:count req)) {:error "count must be present"}
    (< (:count req) 1) {:error "count must be positive"}))

(defn with-validation [handler]
  (fn [req]
    (if-let [error (validate-request req)]
      error
      (handler req))))

(defn with-logging [handler]
  (fn [req]
    (println "Request" req)
    (let [res (handler req)]
      (println "Response" res)
      res)))

(defn test-middleware []
  (let [handler ((comp with-logging with-validation) handler)]
    (handler {})
    (handler {:id 123 :count 12})))
