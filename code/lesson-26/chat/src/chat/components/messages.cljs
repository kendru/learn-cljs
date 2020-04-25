(ns chat.components.messages
  (:require [goog.dom :as gdom]
            [chat.components.component :refer [init-component]]
            [chat.components.helpers :as helpers]
            [chat.state :as state])
  (:import [goog.dom TagName]))

(defn accessor [app]
  (map
    (fn [message]
      (let [user (state/person-by-username app (:sender message))
            name (helpers/display-name user)
            initial (-> name (.charAt 0) (.toUpperCase))
            formatted-timestamp (.toLocaleString
                                  (js/Date. (* 1000 (:timestamp message))))]
        (assoc message :author {:name name
                                :initial initial}
                       :timestamp formatted-timestamp)))
    (:messages app)))

(defn render-message [message]
  (gdom/createDom TagName.ARTICLE "message"
    (gdom/createDom TagName.DIV "message-header"
      (gdom/createDom TagName.DIV "author-avatar" (get-in message [:author :initial]))
      (gdom/createDom TagName.DIV "author-name" (get-in message [:author :name]))
      (gdom/createDom TagName.DIV "message-timestamp" (:timestamp message)))
    (gdom/createDom TagName.DIV "message-content"
      (gdom/createDom TagName.P nil (:content message)))))

(defn render [el messages]
  (doseq [message messages]
    (.appendChild el (render-message message))))

(defn scroll-to-bottom [el]
  (let [observer (js/MutationObserver.
                   #(set! (.-scrollTop el)
                          (.-scrollHeight el)))]
    (.observe observer el #js{"childList" true})))

(defn init-messages []
  (gdom/createDom TagName.SECTION "messages"
    (doto (gdom/createDom TagName.DIV "messages-inner")
      (scroll-to-bottom)
      (init-component :messages accessor render))))
