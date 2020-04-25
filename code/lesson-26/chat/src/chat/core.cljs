(ns chat.core
    (:require [chat.state :as state]
              [chat.api :as api]
              [chat.command :as cmd]
              [chat.components.app :refer [init-app]]
              [chat.handlers]
              [goog.dom :as gdom]))

(enable-console-print!)

(defonce initialized?
  (do
    (cmd/init!)
    (api/init! cmd/cmd-ch)
    (init-app
      (gdom/getElement "app")
      cmd/cmd-ch)
    (set! (.-getAppState js/window) #(clj->js @state/app-state))
    true))
