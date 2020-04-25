(ns chat.components.compose
  (:require [goog.dom :as gdom]
            [chat.command :as cmd])
  (:import [goog.dom TagName]))

(defn init-composer [cmd-ch]
  (let [composer-input (gdom/createDom TagName.TEXTAREA
                         #js{"class" "message-input"})]
    (gdom/createDom TagName.DIV "compose"
      (doto composer-input
        (.addEventListener "keyup"
          (fn [e]
            (when (= (.-key e) "Enter")
              (.preventDefault e)
              (let [content (.-value composer-input)]
                (set! (.-value composer-input) "")
                (cmd/dispatch! cmd-ch :add-message content)))))))))

