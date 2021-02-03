(ns chat.components.messages
  (:require [chat.components.dom :as dom]
            [chat.components.component :refer [init-component]]
            [chat.components.render-helpers :as helpers]
            [chat.state :as state]))

(defn message-state-accessor [app message]
  (let [sender (state/person-by-username app (:sender message))
        name (helpers/display-name sender)
        initial (-> name (.charAt 0) (.toUpperCase))
        formatted-timestamp (.toLocaleString
                              (js/Date. (* 1000 (:timestamp message))))]
    (assoc message :author {:name name
                            :initial initial}
                   :timestamp formatted-timestamp)))

(defn accessor [app]
  (->> app :messages (map #(message-state-accessor app %))))

(defn render-message [message]
  (dom/article "message"
    (dom/div "message-header"
      (dom/div "author-avatar" (get-in message [:author :initial]))
      (dom/div "author-name" (get-in message [:author :name]))
      (dom/div "message-timestamp" (:timestamp message)))
    (dom/div "message-content"
      (dom/p nil (:content message)))))

(defn render [el messages]
  (apply dom/with-children el
    (map render-message messages)))

(defn scroll-to-bottom [el]
  (let [observer (js/MutationObserver.
                   #(set! (.-scrollTop el)
                          (.-scrollHeight el)))]
    (.observe observer el #js{"childList" true})))

(defn init-messages []
  (dom/section "messages"
    (doto (dom/div "messages-inner")
      (scroll-to-bottom)
      (init-component :messages accessor render))))
