(ns learn-cljs.chat.components.sidebar
  (:require [learn-cljs.chat.components.dom :as dom]
            [learn-cljs.chat.components.component :refer [init-component]]
            [learn-cljs.chat.components.render-helpers :as helpers]
            [learn-cljs.chat.message-bus :as bus]
            [goog.events :as gevents]
            [learn-cljs.chat.state :as state]))

(defn sidebar-header [title]
  (dom/div "sidebar-header" title))

(defn render-room [msg-ch room]
  (let [class-name (str "sidebar-item" (when (:active? room) " active"))
        text (:name room)]
    (doto (dom/div class-name text)
      (gevents/listen "click"
        #(bus/dispatch! msg-ch :switch-to-room
           {:id (:id room)})))))

(defn render-create-room [msg-ch el open?]
  (if open?
    (let [add-room-input (dom/input "add-room-input")]
      (dom/with-children el
        (doto add-room-input
          (gevents/listen "keyup"
            #(when (= (.-key %) "Enter")
              (bus/dispatch! msg-ch
                :create-room (.-value add-room-input))))
          (gevents/listen "blur"
            #(bus/dispatch! msg-ch
               :close-create-room-input))))
      (.focus add-room-input))
    (dom/with-children el
      (doto (dom/div "add-room" "Add")
        (gevents/listen "click"
          #(bus/dispatch! msg-ch :open-create-room-input))))))

(defn render-create-room-item [msg-ch]
  (init-component
    (dom/div "sidebar-item no-highlight")
    :sidebar-create-room
    :create-room-input-open?
    (partial render-create-room msg-ch)))

(defn render-rooms [msg-ch el rooms]
  (apply dom/with-children el
    (conj
      (mapv #(render-room msg-ch %) rooms)
      (render-create-room-item msg-ch))))

(defn sidebar-rooms [msg-ch]
   (init-component
     (dom/div "sidebar-rooms")
     :sidebar-rooms
     state/room-list
     (partial render-rooms msg-ch)))

(defn render-person [msg-ch person]
  (let [class-name (str "sidebar-item" (when (:active? person) " active"))
        text (helpers/display-name person)]
    (doto (dom/div class-name text)
      (gevents/listen "click"
        #(bus/dispatch! msg-ch :switch-to-conversation
           {:username (:username person)})))))

(defn render-people [msg-ch el people]
  (apply dom/with-children el
    (map #(render-person msg-ch %) people)))

(defn sidebar-people [msg-ch]
   (init-component
     (dom/div "sidebar-people")
     :sidebar-people
     state/people-list
     (partial render-people msg-ch)))

(defn init-sidebar [msg-ch]
  (dom/aside "sidebar"
    (sidebar-header "Rooms")
    (sidebar-rooms msg-ch)
    (sidebar-header "People")
    (sidebar-people msg-ch)))
