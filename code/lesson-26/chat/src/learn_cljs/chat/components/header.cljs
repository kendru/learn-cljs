(ns learn-cljs.chat.components.header
  (:require [learn-cljs.chat.components.dom :as dom]
            [learn-cljs.chat.components.component :refer [init-component]]
            [learn-cljs.chat.components.render-helpers :refer [display-name]]
            [learn-cljs.chat.state :as state]))

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
                display-name)
     :current-user (:current-user app)}

    :else
    {:title "Welcome to ClojureScript Chat"}))

(defn render [header {:keys [icon title current-user]}]
  (dom/with-children header
    (dom/h1 "view-name"
      (dom/i "material-icons" icon) title)
    (dom/div "user-name"
      (when (some? current-user)
        (display-name current-user)))))

(defn init-header []
  (init-component (dom/header "app-header")
    :header accessor render))
