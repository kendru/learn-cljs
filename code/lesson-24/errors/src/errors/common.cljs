(ns errors.common)

;; These functions are stubs to make the example work

(defn display-error [msg]
  (js/console.error "TODO: Display error:" msg))

(defn display-message [msg]
  (js/console.log "TODO: Display info mesaahe:" msg))

(defn log-error [err-data]
  (js/console.log "TODO: Log error:" (clj->js err-data)))

(defn update-field-errors [validation-data]
  (js/console.log "TODO: Update UI with validation errors:" (clj->js validation-data)))

(defn initialize-user []
  (js/console.log "TODO: initialize a new user")
  {:id 123
   :name "Fake user"})
