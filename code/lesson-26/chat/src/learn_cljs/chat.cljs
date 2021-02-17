(ns learn-cljs.chat
    (:require [learn-cljs.chat.state :as state]
              [learn-cljs.chat.api :as api]
              [learn-cljs.chat.message-bus :as bus]
              [learn-cljs.chat.components.app :refer [init-app]]
              [learn-cljs.chat.handlers]
              [goog.dom :as gdom]))

(defonce initialized?
  (do
    (api/init! bus/msg-ch js/WS_API_URL)
    (init-app
      (gdom/getElement "app")
      bus/msg-ch)
    (set! (.-getAppState js/window) #(clj->js @state/app-state))
    true))
