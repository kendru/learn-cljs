(ns notes.state
  (:require [reagent.core :as r]
            [notes.events :as events]))

(def initial-state
  {:current-route [:home]
   :search-input ""
   :data {:notes {:order-by :created-at
                  :order-dir :desc}
          :tags {:order-by :name
                 :order-dir :asc}}
   :notifications {:messages []
                   :next-id 0}})

(defonce app (r/atom initial-state))

(def handlers (atom {}))

(defn register-handler! [event-type handler-fn]
  (swap! handlers assoc event-type handler-fn))

(events/register-listener!
 (fn [type payload]
   (when-let [handler-fn (get @handlers type)]
     (swap! app #(handler-fn  % payload)))))
