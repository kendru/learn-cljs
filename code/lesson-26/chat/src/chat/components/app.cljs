(ns chat.components.app
  (:require [chat.components.sidebar :refer [init-sidebar]]
            [chat.components.header :refer [init-header]]
            [chat.components.messages :refer [init-messages]]
            [chat.components.compose :refer [init-composer]]
            [chat.components.auth :refer [init-auth]]
            [goog.dom :as gdom])
  (:import [goog.dom TagName]))

(defn init-main [cmd-ch]
  (doto (gdom/createDom TagName.SECTION "content-main")
    (.appendChild (init-header))
    (.appendChild (init-messages))
    (.appendChild (init-composer cmd-ch))))

(defn init-app [el cmd-ch]
  (let [wrapper (gdom/createDom TagName.DIV "app-wrapper"
                  (init-sidebar cmd-ch)
                  (init-main cmd-ch)
                  (init-auth cmd-ch))]
    (set! (.-innerText el) "")
    (.appendChild el wrapper)))
