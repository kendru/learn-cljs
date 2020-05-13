(ns chat.components.compose
  (:require [chat.components.dom :as dom]
            [chat.message-bus :as bus]))

(defn init-composer [msg-ch]
  (let [composer-input (dom/textarea "message-input")]
    (.addEventListener composer-input "keyup"
      (fn [e]
        (when (= (.-key e) "Enter")
          (.preventDefault e)
          (let [content (.-value composer-input)]
            (set! (.-value composer-input) "")
            (bus/dispatch! msg-ch :add-message content)))))
    (dom/div "compose" composer-input)))


