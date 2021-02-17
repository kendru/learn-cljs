(ns learn-cljs.chat.components.app
  (:require [learn-cljs.chat.components.sidebar :refer [init-sidebar]]
            [learn-cljs.chat.components.header :refer [init-header]]
            [learn-cljs.chat.components.messages :refer [init-messages]]
            [learn-cljs.chat.components.compose :refer [init-composer]]
            [learn-cljs.chat.components.auth :refer [init-auth]]
            [learn-cljs.chat.components.dom :as dom]))

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
