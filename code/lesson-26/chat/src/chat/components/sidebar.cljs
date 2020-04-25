(ns chat.components.sidebar
  (:require [chat.components.component :refer [init-component]]
            [chat.components.helpers :as helpers]
            [chat.command :as cmd]
            [goog.dom :as gdom]
            [goog.events :as gevents]
            [chat.state :as state])
  (:import [goog.dom TagName]))

(defn sidebar-header [title]
  (gdom/createDom TagName.DIV "sidebar-header" title))

(defn render-room [cmd-ch room]
  (let [class-name (str "sidebar-item" (when (:active? room) " active"))
        text (:name room)]
    (doto (gdom/createDom TagName.DIV class-name text)
      (.addEventListener "click"
        #(cmd/dispatch! cmd-ch :switch-to-room
           {:id (:id room)})))))

(defn render-create-room [cmd-ch el open?]
  (if open?
    (let [add-room-input (gdom/createDom TagName.INPUT "add-room-input")]
      (.appendChild el
        (doto add-room-input
          (.addEventListener "keyup"
            #(when (= (.-key %) "Enter")
              (cmd/dispatch! cmd-ch :create-room (.-value add-room-input))))
          (.addEventListener "blur"
            #(cmd/dispatch! cmd-ch :close-create-room-input))))
      (.focus add-room-input))
    (.appendChild el
      (doto (gdom/createDom TagName.DIV "add-room" "Add")
        (.addEventListener "click"
          #(cmd/dispatch! cmd-ch :open-create-room-input))))))

(defn render-create-room-item [cmd-ch]
  (doto (gdom/createDom TagName.DIV "sidebar-item no-highlight")
    (init-component :sidebar-create-room
      :create-room-input-open?
      (partial render-create-room cmd-ch))))

(defn render-rooms [cmd-ch el rooms]
  (doseq [room rooms]
    (.appendChild el
      (render-room cmd-ch room)))
  (.appendChild el
    (render-create-room-item cmd-ch)))

(defn sidebar-rooms [cmd-ch]
  (doto (gdom/createDom TagName.DIV "sidebar-rooms")
        (init-component :sidebar-rooms
          state/room-list
          (partial render-rooms cmd-ch))))

(defn render-person [cmd-ch person]
  (let [class-name (str "sidebar-item" (when (:active? person) " active"))
        text (helpers/display-name person)]
    (doto (gdom/createDom TagName.DIV class-name text)
      (.addEventListener "click"
        #(cmd/dispatch! cmd-ch :switch-to-conversation
           {:username (:username person)})))))

(defn render-people [cmd-ch el people]
  (doseq [person people]
    (.appendChild el
      (render-person cmd-ch person))))

(defn sidebar-people [cmd-ch]
  (doto (gdom/createDom TagName.DIV "sidebar-people")
        (init-component :sidebar-people
          state/people-list
          (partial render-people cmd-ch))))

(defn init-sidebar [cmd-ch]
  (doto (gdom/createDom TagName.ASIDE "sidebar")
    (.appendChild (sidebar-header "Rooms"))
    (.appendChild (sidebar-rooms cmd-ch))
    (.appendChild (sidebar-header "People"))
    (.appendChild (sidebar-people cmd-ch))))
