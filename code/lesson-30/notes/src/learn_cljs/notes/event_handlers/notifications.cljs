(ns learn-cljs.notes.event-handlers.notifications
  (:require [learn-cljs.notes.state :refer [register-handler!]]
            [learn-cljs.notes.command :refer [dispatch!]]))

(register-handler!
 :notification/added
 (fn [db payload]
   (let [{:keys [type text]
          :or {type "info"}} payload
         id (get-in db [:notifications :next-id])]
     (js/setTimeout #(dispatch! :notification/remove id) 10000)
     (-> db
         (update-in [:notifications :messages]
                    conj {:id id
                          :type type
                          :text text})
         (update-in [:notifications :next-id] inc)))))

(register-handler!
 :notification/removed
 (fn [db id]
   (update-in db [:notifications :messages]
              (fn [messages]
                (filterv #(not= id (:id %)) messages)))))
