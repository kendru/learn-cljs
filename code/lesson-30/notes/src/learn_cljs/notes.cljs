(ns learn-cljs.notes
  (:require [learn-cljs.notes.ui.header :refer [header]]
            [learn-cljs.notes.ui.main :refer [main]]
            [learn-cljs.notes.ui.sidebar :refer [sidebar]]
            [learn-cljs.notes.ui.footer :refer [footer]]
            [learn-cljs.notes.ui.notifications :refer [notifications]]
            [learn-cljs.notes.routes :as routes]
            [learn-cljs.notes.event-handlers.core]
            [reagent.dom :as rdom]
            [goog.dom :as gdom]))

(enable-console-print!)

(defn app []
  [:div.app
   [header]
   [main]
   [sidebar]
   [footer]
   [notifications]])

(rdom/render
 [app]
 (gdom/getElement "app"))

(defonce initialized?
  (do
    (routes/initialize!)
    true))
