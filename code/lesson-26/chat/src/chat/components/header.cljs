(ns chat.components.header
  (:require [goog.dom :as gdom]
            [chat.components.component :refer [init-component]]
            [chat.components.helpers :as helpers]
            [chat.state :as state])
  (:import [goog.dom TagName]))

(defn accessor [app]
  (cond
    (state/is-current-view-room? app)
    {:icon "meeting_room"
     :title (-> app
                (get-in [:current-view :id])
                (->> (state/room-by-id app))
                :name)
     :current-user (:current-user app)}

    (state/is-current-view-conversation? app)
    {:icon "person"
     :title (-> app
                (get-in [:current-view :username])
                (->> (state/person-by-username app))
                helpers/display-name)
     :current-user (:current-user app)}

    :else
    {:title "Welcome to ClojureScript Chat"}))

(defn render [header {:keys [icon title current-user]}]
  (doto header
    (.appendChild
      (gdom/createDom TagName.H1 "view-name"
        (gdom/createDom TagName.I "material-icons" icon)
        title))
    (.appendChild
      (gdom/createDom TagName.DIV "user-name"
        (when current-user
          (helpers/display-name current-user))))))

(defn init-header []
  (doto (gdom/createDom TagName.HEADER "app-header")
        (init-component :header accessor render)))
