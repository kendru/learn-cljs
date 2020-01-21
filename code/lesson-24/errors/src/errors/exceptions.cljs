(ns errors.exceptions
  (:require [errors.common :refer [display-error log-error update-field-errors]]))

(def required-attrs [:id :email])
(def allowed-attrs [:id :email :first-name :last-name])

(defn make-user [user-data]
  (cond
    (not (every? #(contains? user-data %) required-attrs))
    (throw (ex-info "Missing required attributes"
                    {:required required-attrs
                     :found (keys user-data)}
                    :validation-failed))
    (not (every? (set allowed-attrs) (keys user-data)))
    (throw (ex-info "Found disallowed attributes"
                    {:allowed allowed-attrs
                     :found (keys user-data)}
                    :validation-failed))
    :else (assoc user-data :type :user)))

(defn hydrate-user []
  (let [serialized-user (try
                          (.getItem js/localStorage "current-user")
                          (catch js/Error _
                            (throw (ex-info "Could not load data from localStorage"
                                            {}
                                            :local-storage-unsupported))))
        user-data (try
                    (.parse js/JSON serialized-user)
                    (catch js/Error _
                      (throw (ex-info "Could not parse user data"
                                      {:string serialized-user}
                                      :parse-failed))))]
    (-> user-data
        (js->clj :keywordize-keys true)
        make-user)))

(defn run []
  (try
    (hydrate-user)
    (catch ExceptionInfo e
      (case (ex-cause e)
        :local-storage-unsupported
        (display-error (str "Local storage not supported: "
                                                      (ex-message e)))
        :parse-failed
        (do (display-error "Could not load user data from browser")
            (log-error {:type :user-parse-failed
                        :source (:string (ex-data e))}))
        :validation-failed
        (do (display-error "There was an error in your submission. Please correct it before continuing.")
            (update-field-errors (ex-data e)))
        ;; Re-throw an unknown error
        (throw e)))))
