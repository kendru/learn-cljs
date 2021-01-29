(ns notes.event-handlers.routes
  (:require [notes.state :refer [register-handler!]]))

(defn- note-for-edit-route [db route-params]
  (let [note-id (get-in route-params [1 :note-id])
        note-id (js/parseInt note-id)]
    (get-in db [:data :notes note-id])))

(register-handler!
 :route/navigated
 (fn [db route-params]
   (cond-> db
     true (assoc :current-route route-params)

     (= :create-note (first route-params))
     (assoc :note-form {})

     (= :edit-note (first route-params))
     (assoc :note-form (note-for-edit-route db route-params)))))
