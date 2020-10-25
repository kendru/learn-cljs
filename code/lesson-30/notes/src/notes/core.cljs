(ns notes.core
  (:require [notes.ui.sidebar :refer [sidebar]]
            [notes.ui.main :refer [main]]
            [notes.api :as api]
            [notes.routes]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [goog.dom :as gdom]))

(enable-console-print!)

(defn app []
  [:div.app
    [sidebar]
    [main]])

(rdom/render
  [app]
  (gdom/getElement "app"))

(defonce inititialized?
  (do
    (api/init! js/WS_API_URL)
    true))
