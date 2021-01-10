(ns notes.event-handlers.routes
  (:require [notes.state :refer [register-handler!]]))

(register-handler!
 :route/navigated
 (fn [db route-params]
   (assoc db :current-route route-params)))
