(ns errors.conditions
  (:require [special.core :refer [condition manage]]
            [errors.common :refer [display-error display-message initialize-user]]))

(defn get-localstorage [key]
  (try
    (.getItem js/localStorage key)
    (catch js/Error _
      (condition :localstorage-unsupported nil))))

(defn get-parsed-data [key]
  (let [serialized (get-localstorage key)]
    (try
      (if-let [parsed (js->clj
                        (.parse js/JSON serialized)
                        :keywordize-keys true)]
        parsed
        (condition :no-data key
          :normally {}))
      (catch js/Error _
        (condition :parse-error {:key key :string serialized}
          :normally {}
          :reparse #(get-parsed-data %))))))

(defn hydrate-user []
  (let [managed-fn (manage get-parsed-data
                     :localstorage-unsupported (fn [_]
                                                 (display-error "Unsupported")
                                                 "{}")
                     :parse-error (fn [{:keys [key]}]
                                    (if (= key "current-user")
                                      (condition :reparse "currUser")
                                      (do (display-error "Cannot parse")
                                          (initialize-user))))

                     :no-data (fn [_]
                                (display-message "")
                                (initialize-user)))]
    (managed-fn "current-user")))

(defn run []
  (let [f (fn [s]
            (if (= 0 (mod (count s) 2))
              (condition :even-length s
                :normally "EVEN"
                :shout (.toUpperCase s))
              (str "You said: " s)))
        managed (manage f
                  :even-length (fn [s]
                                (if (= "loud" s)
                                  (condition :shout)
                                  (str s "!"))))]
    (println
      [(managed "test")
       (managed "foo")
       (managed "loud")]))
  (println "Hydrated user:" (hydrate-user)))
