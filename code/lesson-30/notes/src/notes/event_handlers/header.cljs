(ns notes.event-handlers.header
  (:require [notes.state :refer [register-handler!]]))

(register-handler!
 :search/input-updated
 (fn [db text]
   (assoc-in db [:search-input] text)))

(register-handler!
 :search/input-cleared
 (fn [db _]
   (assoc-in db [:search-input] "")))
