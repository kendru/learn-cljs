(ns estimates.core
  (:require [estimates.ui.sidebar :refer [sidebar]]
            [estimates.ui.main :refer [main]]
            [estimates.api :as api]
            [estimates.routes]
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
