(ns chat.components.app
  (:require [chat.components.sidebar :refer [init-sidebar]]
            [chat.components.header :refer [init-header]]
            [chat.components.messages :refer [init-messages]]
            [chat.components.compose :refer [init-composer]]
            [chat.components.auth :refer [init-auth]]
            [chat.components.dom :as dom]))

(defn init-main [msg-ch]
  (dom/section "content-main"
    (init-header)
    (init-messages)
    (init-composer msg-ch)))

(defn init-app [el msg-ch]
  (let [wrapper (dom/div "app-wrapper"
                  (init-sidebar msg-ch)
                  (init-main msg-ch)
                  (init-auth msg-ch))]
    (set! (.-innerText el) "")
    (.appendChild el wrapper)))
