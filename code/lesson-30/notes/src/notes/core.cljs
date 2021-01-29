(ns notes.core
  (:require [notes.ui.header :refer [header]]
            [notes.ui.main :refer [main]]
            [notes.ui.sidebar :refer [sidebar]]
            [notes.ui.footer :refer [footer]]
            [notes.ui.notifications :refer [notifications]]
            [notes.routes :as routes]
            [notes.event-handlers.core]
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
