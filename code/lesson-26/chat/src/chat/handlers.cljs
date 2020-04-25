(ns chat.handlers
  (:require [chat.command :as cmd]
            [chat.api :as api]
            [chat.state :as state]))

(defn should-set-message? [username room]
  (let [app @state/app-state]
    (or
      (and (some? username)
          (state/is-current-view-conversation? app)
          (= username (state/current-conversation-recipient app)))
      (and (some? room)
          (state/is-current-view-room? app)
          (= room (state/current-room-id app))))))

(defonce is-initialized?
  (do
    ;;;;;;;;;;;;;;;;;;;;;
    ;; Sidebar

    (cmd/handle! cmd/cmd-bus :switch-to-conversation
      (fn [{:keys [username]}]
        (api/send! :set-view {:type :conversation, :username username})
        (swap! state/app-state
          #(-> %
               (state/switched-to-conversation username)
               (state/messages-cleared)))))

    (cmd/handle! cmd/cmd-bus :switch-to-room
      (fn [{:keys [id]}]
        (api/send! :set-view {:type :room, :id id})
        (swap! state/app-state
          #(-> %
               (state/switched-to-room id)
               (state/messages-cleared)))))

    ;;;;;;;;;;;;;;;;;;;;;
    ;; Composer

    (cmd/handle! cmd/cmd-bus :add-message
      (fn [content]
        (api/send! :add-message {:content content})))

    ;;;;;;;;;;;;;;;;;;;;;
    ;; People

    (cmd/handle! cmd/cmd-bus :api/people-listed
      (fn [people]
        (swap! state/app-state state/received-people-list people)))

    (cmd/handle! cmd/cmd-bus :api/person-joined
      (fn [person]
        (swap! state/app-state state/person-joined person)))

    (cmd/handle! cmd/cmd-bus :api/person-left
      (fn [username]
        (swap! state/app-state state/person-left username)))

    ;;;;;;;;;;;;;;;;;;;;;
    ;; Rooms

    (cmd/handle! cmd/cmd-bus :open-create-room-input
      (fn []
        (swap! state/app-state state/create-room-input-opened)))

    (cmd/handle! cmd/cmd-bus :close-create-room-input
      (fn []
        (swap! state/app-state state/create-room-input-closed)))

    (cmd/handle! cmd/cmd-bus :create-room
      (fn [name]
        (api/send! :create-room {:name name})))

    (cmd/handle! cmd/cmd-bus :api/rooms-listed
      (fn [rooms]
        (swap! state/app-state state/received-rooms-list rooms)
        (when-let [first-room (first rooms)]
          (cmd/dispatch! cmd/cmd-ch :switch-to-room
            {:id (:id first-room)}))))

    (cmd/handle! cmd/cmd-bus :api/room-created
      (fn [room]
        (swap! state/app-state
          #(-> %
               (state/room-added room)
               (state/create-room-input-closed)))))

    ;;;;;;;;;;;;;;;;;;;;;
    ;; Messages

    (cmd/handle! cmd/cmd-bus :api/message-received
      (fn [{:keys [message username room]}]
        (when (should-set-message? username room)
          (swap! state/app-state state/message-received message))))

    (cmd/handle! cmd/cmd-bus :api/messages-received
      (fn [{:keys [messages username room]}]
        (when (should-set-message? username room)
          (swap! state/app-state state/messages-received messages))))

    ;;;;;;;;;;;;;;;;;;;;;
    ;; Auth

    (cmd/handle! cmd/cmd-bus :toggle-auth-modal
      (fn []
        (swap! state/app-state state/auth-modal-toggled)))

    (cmd/handle! cmd/cmd-bus :sign-in
      (fn [data]
        (api/send! :sign-in data)))

    (cmd/handle! cmd/cmd-bus :sign-up
      (fn [data]
        (api/send! :sign-up data)))

    (cmd/handle! cmd/cmd-bus :api/authenticated
      (fn [user-info]
        (swap! state/app-state state/user-authenticated user-info)
        (api/send! :list-people)
        (api/send! :list-rooms)))

    true))
