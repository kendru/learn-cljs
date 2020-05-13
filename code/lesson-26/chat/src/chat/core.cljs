(ns chat.core
    (:require [chat.state :as state]
              [chat.api :as api]
              [chat.message-bus :as bus]
              [chat.components.app :refer [init-app]]
              [chat.handlers]
              [goog.dom :as gdom]))

(enable-console-print!)

(defonce initialized?
  (do
    (api/init! bus/msg-ch js/WS_API_URL)
    (init-app
      (gdom/getElement "app")
      bus/msg-ch)
    (set! (.-getAppState js/window) #(clj->js @state/app-state))
    true))
