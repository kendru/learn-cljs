---
title: "Capstone 4 - Group Chat | Lesson 26"
date: 2020-04-26T11:57:50-06:00
type: "docs"
opengraphImage: "https://www.learn-clojurescript.com/img/lesson26/cljs-chat-screenshot.png"
---

# Lesson 26: Capstone 4 - Group Chat

Congratulations! At this point, we have learned enough ClojureScript to write pretty much any sort of app that we would like. Sure, over the coming chapters, we will pick up some tools and techniques that will make us more productive, but nothing is stopping us from writing complete, production-quality apps with what we have learned so far. For this capstone, we will be writing a realtime group chat application - similar to a very slimmed-down version of Slack. You can connect to a live instance of this application at https://chat.learn-cljs.com/.

---

*In This Lesson:*

- Design a non-trivial application in terms of state, events, and components
- Interact with a WebSocket API
- Learn some principles for refactoring

---

![ClojureScript Chat Screenshot](/img/lesson26/cljs-chat-screenshot.png)

_Screenshot of ClojureScript Chat_

## Thinking About Interactions

There are many ways to start building an application, and no one way is necessarily best. However, for ClojureScript, a natural place to start is by thinking about the state and how we want the user to interact with that state. At a high level, we will have 2 types of data that we want to keep track of: _application data_ and _UI state_. Application data is any data that we receive from the server that powers the application UI state, on the other hand, is made up of pieces of data that are never persisted but are useful for determining what state various components are in.

### Application Data

For our chat application, we will keep a vector of `rooms` that we can join, a vector of `people` that we can enter into conversations with, and a vector of `messages` that we can read in the current room or conversation. We will also want to keep track of the current user for a couple of reasons: first, we will display the user's name in the upper right-hand corner of the screen, and second, if there is no user, we will display a modal so that the user can sign up or sign in.

```clojure
(ns chat.state)

(def initial-state                                         ;; <1>
  {:rooms []
   :people []
   :messages []
   :current-user nil})

(defonce app-state (atom initial-state))                   ;; <2>
```

_state.cljs_

1. Define the initial application state as an immutable map
2. Define the app state as an atom whose starting value is the same as `initial-state`

We will next add functions to transform the application data such that our UI components can easily consume it. We will also provide functions that transition the app state as well. In the interest of writing pure and testable code wherever possible, our functions that update the app state will take in an immutable `state` and return a new state (rather than mutating the `app-state` atom directly). These functions will be invoked when we receive a response from the API.

```clojure
;; Application data queries
(defn room-by-id [state id]
  (->> state
       :rooms
       (filter #(= id (:id %)))
       first))

(defn person-by-username [state username]
  (->> state
       :people
       (filter #(= username (:username %)))
       first))

;; Application data transition functions
(defn received-people-list [state people]
  (assoc state :people people))

(defn person-joined [state person]
  (let [username (:username person)
        is-joined-user? #(= username (:username %))]
    (update state :people
      (fn [people]
        (if (some is-joined-user? people)
          (map
            (fn [user]
              (if (is-joined-user? user)
                (assoc user :online? true)
                user))
            people)
          (conj people person))))))

(defn person-left [state username]
  (update state :people
    (fn [people]
      (map #(if (= username (:username %))
              (assoc % :online? false)
              %) people))))

(defn received-rooms-list [state rooms]
  (assoc state :rooms rooms))

(defn room-added [state room]
  (update state :rooms conj room))

(defn message-received [state message]
  (update state :messages conj message))

(defn messages-received [state messages]
  (assoc state :messages messages))

(defn messages-cleared [state]
  (assoc state :messages []))
```

Without going into too much detail, the query functions allow us to look up a user by username or a room by ID. The transition functions handle most of the responses that we can expect from the API. One interesting piece of logic is the `person-joined` function, which either marks a previously seen user as "online" or adds a brand new user to the user list. Additionally, the `messages-cleared` function is one that will be invoked by our UI (rather than the API) whenever the user switches between rooms or conversations so that we do not see messages from the previous room/conversation while we wait for the server to send us a new message list. In roughly 40 lines of code we have defined the interface for interacting with application data.

### UI State

Since this is a simple application, we only need to keep a few pieces of application state:

- The current "view" - i.e. the room or conversation that the user has focused
- A toggle determining whether to display the "Sign In" or "Sign Up" modal before the user has authenticated
- A flag indicating whether the "Create Room" input is open

Many applications keep input data in the UI state, but until we start building on top of React, this would introduce too much complexity to justify it. For this project, we will simply query the DOM to get the values of user input fields.

First, we will add these fields to the application state:

```clojure
(def initial-state
  {;; ...
   :current-view nil
   ;; May be {:type :room, :id 123}
   ;;     or {:type :conversation, :username "user_abc"}

   :auth-modal :sign-in
   ;; May be :sign-in
   ;;     or :sign-up

   :create-room-input-open? false})
```

Next, we will add query and state transition functions, just like we did for the application data:

```clojure
;; UI state queries
(defn is-current-view-room? [state]
  (= ::room (get-in state [:current-view :type])))

(defn current-room-id [state]
  (get-in state [:current-view :id]))

(defn is-current-view-conversation? [state]
  (= ::conversation (get-in state [:current-view :type])))

(defn current-conversation-recipient [state]
  (get-in state [:current-view :username]))

(defn room-list [state]
  (let [current-room (when (is-current-view-room? state)
                       (get-in state [:current-view :id]))]
    (map (fn [room]
           (assoc room
             :active? (= current-room (:id room))))
         (:rooms state))))

(defn people-list [app]
  (let [current-username (when (is-current-view-conversation? app)
                           (get-in app [:current-view :username]))]
    (map (fn [person]
           (assoc person
             :active? (= current-username (:username person))))
         (:people app))))

;; UI state transition functions
(defn switched-to-room [state room-id]
  (assoc state :current-view {:type ::room
                              :id room-id}))

(defn switched-to-conversation [state username]
  (assoc state :current-view {:type ::conversation
                              :username username}))

(defn auth-modal-toggled [state]
  (update state :auth-modal
    {:sign-up :sign-in                                     ;; <1>
     :sign-in :sign-up}))

(defn user-authenticated [state user]
  (assoc state :current-user user))

(defn create-room-input-opened [state]
  (assoc state :create-room-input-open? true))

(defn create-room-input-closed [state]
  (assoc state :create-room-input-open? false))
```

1. Use a map itself as a function that maps the current auth model state to the next state

With our entire state interface defined, let's move on to the mechanism through which our components and API will interact with our state: the message bus.

### Message Bus Pattern

Rather than mutating the application state directly from our components or the API, we will introduce a messaging layer that will allow us to more easily test our components and will give us the ability for one component to potentially react to a message from another. Building on the knowledge of `core.async` from the previous lesson, we will create a very simple messaging system. This messaging system will allow us to dispatch messages with a given type from anywhere in our app and subscribe functions to handle messages of a given type.

```clojure
(ns chat.message-bus
  (:require [cljs.core.async :refer [go-loop pub sub chan <! put!]]))

(def msg-ch (chan 1))                                      ;; <1>
(def msg-bus (pub msg-ch ::type))                          ;; <2>

(defn dispatch!                                            ;; <3>
 ([ch type] (dispatch! ch type nil))
 ([ch type payload]
  (put! ch {::type type
            ::payload payload})))

(defn handle! [p type handle]                              ;; <4>
  (let [sub-ch (chan)]
    (sub p type sub-ch)
    (go-loop []
      (handle (::payload (<! sub-ch)))
      (recur))))
```

_learn\_cljs/chat/message\_bus.cljs_

1. Channel on which messages will be dispatched
2. Publication that will allow consumers to receive messages from `msg-ch`
3. Function for dispatching a typed message
4. Function for registering a handler for a type of message.

![Architecture of the Messaging Layer](/img/lesson26/messaging-layer.png)

_Architecture of the Messaging Layer_

This simple messaging layer provides pub/sub capability where we can use `dispatch!` to emit messages onto the `msg-ch` channel and `handle!` to subscribe a callback to be called whenever messages of a given type are dispatched. While we could have hard-coded `dispatch!` to put messages on `msg-ch` and `handle!` to subscribe to `msg-bus`, but once again, this would make our code much more difficult to test and much less modular.

## Building Components

Our application is fairly simple, but it consists of several distinct layout components:

- header
- sidebar
- message list
- message composer
- authorization modal

We will break out each of these high-level layout components into a namespace, and we will also include a top-level "app" component that initializes the rest of the layout.

![Diagram of High-Level Component Layout](/img/lesson26/app-layout.png)

_Application Layout_

Most of our components will follow the same pattern: they will mount into a parent DOM node, watch a portion of the application state (or some value computed from the state) for change, and re-render themselves the a change occurs. Let's go ahead and create a function that will allow us to initialize a component that follows this pattern:

```clojure
(ns chat.components.component
  (:require [chat.state :as state]))

(defn init-component
  "Initialize a component.
  Parameters:
  el - Element in which to render component
  watch-key - Key that uniquely identifies this component
  accessor - Function that takes the app state and returns the
             component state
  render - Function that takes the parent element and component
           state and renders DOM"
  [el watch-key accessor render]
  (add-watch state/app-state watch-key                     ;; <1>
    (fn [_ _ old new]
      (let [state-old (accessor old)                       ;; <2>
            state-new (accessor new)]
        (when (not= state-old state-new)                   ;; <3>
          (set! (.-innerText el) "")
          (render el state-new)))))
  (render el (accessor @state/app-state))                  ;; <4>
  el)                                                      ;; <5>
```

_components/component.cljs_

1. Watch the app state for all changes
2. Use the supplied `accessor` function to compute the old and new app state
3. Only re-render if the component state changed
4. Perform an initial render
5. Return the parent component

The use of this utility function will become clear as we start building components in the next section.

### Application Chrome

We will build the UI for this application in a top-down fashion, starting with the application container, followed by the "chrome" components - that is the header and sidebar - before moving on to lower level pieces. For the moment, let's create an app container that simply loads the header and renders it into the DOM:

```clojure
(ns chat.components.app
  (:require [chat.components.header :refer [init-header]]
            [goog.dom :as gdom])
  (:import [goog.dom TagName]))

(defn init-main []
  (gdom/createDom TagName.SECTION "content-main"
    (init-header)))

(defn init-app [el msg-ch]
  (let [wrapper (gdom/createDom TagName.DIV "app-wrapper"
                  (init-main))]
    (set! (.-innerText el) "")
    (.appendChild el wrapper)))
```

_components/app.cljs_


This application container code is fairly straightforward: we create a basic shell with a couple of DOM nodes then call `render-header` to create and return the DOM necessary for the header. Before this code does anything useful, we will need to create a `chat.components.header` namespace that exposes the `init-header` function. We'll do that now:

```clojure
(ns chat.components.header
  (:require [goog.dom :as gdom]
            [chat.components.component :refer [init-component]]
            [chat.state :as state])
  (:import [goog.dom TagName]))

(defn display-name [person]                                ;; <1>
  (if person
    (->> person
        ((juxt :first-name :last-name))
        (s/join " "))
    "REMOVED"))

(defn accessor [app]                                       ;; <2>
  (cond
    (state/is-current-view-room? app)                      ;; <3>
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

    :else                                                  ;; <4>
    {:title "Welcome to ClojureScript Chat"}))

(defn render [header {:keys [icon title current-user]}]    ;; <5>
  (doto header
    (.appendChild
      (gdom/createDom TagName.H1 "view-name"
        (gdom/createDom TagName.I "material-icons" icon)
        title))
    (.appendChild
      (gdom/createDom TagName.DIV "user-name"
        (when (some? current-user)
          (display-name current-user))))))

(defn init-header []                                       ;; <6>
  (init-component
    (gdom/createDom TagName.HEADER "app-header")
    :header accessor render))
```

_components/header.cljs_

1. Helper function for displaying a formatted version of a user's name
2. Accessor function that takes the app state and computes our component state
3. Use the functions that we wrote in `chat.state` to access the relevant data
4. Provide a fallback if the user is not in a chat room or a conversation
5. Render function that updates the `header` element based on app state
6. Create the header component and return its DOME element

Here we see the `init-component` function in action: within `init-header`, we create an element to render the header content into, and we pass that element, along with an accessor function that computes component state from application state and a render function that will update our header whenever the component state changes. One nice feature of the way that we designed our `init-component` helper is that the render function will only be called if the app state changes in a way that affects how the header renders. When we get to the next section, we will rely on React to optimize the rendering cycle for us, but it is instructive to see how easily we can build a UI without any framework.

Before moving on, let's clean things up a bit. First, the `display-name` function will be useful for rendering user names in several places, so we can go ahead and refactor that function into a `render-helpers` namespace:

```clojure
;; components/render_helpers.cljs
(ns chat.components.render-helpers
  (:require [clojure.string :as s]))

(defn display-name [person]
  (if person
    (->> person
        ((juxt :first-name :last-name))
        (s/join " "))
    "REMOVED"))

;; components/header.cljs
(ns chat.components.header
  (:require ; ...
            [chat.components.render-helpers :refer [display-name]])
  ; ...
)
```

Additionally, the syntax for the `goog.dom` library can be a bit verbose, so we will create another `dom` helper namespace that will allow us to write code like this:

```clojure
(dom/h1 "title" "Hello world!")
```

instead of this:

```clojure
(gdom/createDom TagName.H1 "title" "Hello world!")
```

We will create this helper namespace as `chat.components.dom`.

```clojure
(ns chat.components.dom
  (:require [goog.dom :as gdom])
  (:import [goog.dom TagName]))

(defn dom-fn [tag-name]                                    ;; <1>
  (fn [& args]
    (apply gdom/createDom tag-name args)))

(def div (dom-fn TagName.DIV))
;; ...                                                     ;; <2>

(defn with-children [el & children]                        ;; <3>
  (doseq [child children]
    (.appendChild el child))
  el)
```

_components/dom.cljs_

1. Higher-order function returning a function that creates a DOM element
2. Define a function for each DOM element that we will use. Most of the elements have been omitted for brevity.
3. Define another helper that cleans up repeated use of `.appendChild`

Then, back in `header.cljs`, we can update our render and initialization functions to use this new DOM utility:

```clojure
(ns chat.components.header                                 ;; <1>
  (:require ; ...
            [chat.components.dom :as dom]))

;; ...

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
```

1. Remove require of `[goog.dom :as gdom]` and import of `[goog.dom TagName]`

Now that we have refactored our code to make it more readable and concise, let's move on to the sidebar. The sidebar will display a list of rooms, a list of users that we can converse with, and a control for creating a new room. Clicking on either a room name or a user's name should switch to that room or conversation respectively. Unlike the header, the sidebar contains elements that the user should be able to interact with in order to update the application state. For this reason, we will pass the message channel down through the component hierarchy to all components that need it, and we will call the `chat.message-bus/dispatch!` function to send off our messages. The messages will be processed by any handler that we have registered, and eventually, some of them will trigger API requests.

There is not anything that is novel in this code: we initialize components that receive application state, manage their own portion of the DOM, and add event listeners.  Without further ado, the entire code for the sidebar is listed below:

```clojure
(ns chat.components.sidebar
  (:require [chat.components.dom :as dom]                  ;; <1>
            [chat.components.component :refer [init-component]]
            [chat.components.render-helpers :as helpers]
            [chat.message-bus :as bus]
            [goog.events :as gevents]
            [chat.state :as state]))

(defn sidebar-header [title]
  (dom/div "sidebar-header" title))

(defn render-room [msg-ch room]
  (let [class-name (str "sidebar-item" (when (:active? room)
                                         " active"))
        text (:name room)]
    (doto (dom/div class-name text)
      (gevents/listen "click"                              ;; <2>
        #(bus/dispatch! msg-ch :switch-to-room
           {:id (:id room)})))))

(defn render-create-room [msg-ch el open?]                 ;; <3>
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
      (.focus add-room-input))                             ;; <4>
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
  (apply dom/with-children el                              ;; <5>
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
  (let [class-name (str "sidebar-item" (when (:active? person)
                                          " active"))
        text (helpers/display-name person)]
    (doto (dom/div class-name text)
      (gevents/listen "click"
        #(bus/dispatch! msg-ch :switch-to-conversation
           {:username (:username person)})))))

(defn render-people [msg-ch el people]
  (dom/with-children el
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
```

_chat/components/sidebar.cljs_

1. Require the UI helpers that we just factored out of the header code
2. Add an event listener to each room in the list that we render
3. Render the "Add Room" widget that turns into an input on click
4. Automatically focus the input field whenever "Add Room" is clicked
5. Since `dom/with-children` expects a variadic argument list of children, we use `apply` to "unwrap" the vector of elements that we are passing in

We need to initialize this sidebar in `components/app.cljs` as well:

```clojure
(ns chat.components.app
  (:require ;; ...
            [chat.components.sidebar                       ;; <1>
             :refer [init-sidebar]]))

(defn init-app [el msg-ch]
  (let [wrapper (dom/div "app-wrapper"                     ;; <2>
                  (init-sidebar msg-ch)                    ;; <3>
                  ;; ...
                )]
    ;; ...
  ))
```

1. Require the initialization function for the sidebar
2. Refactor `app.cljs` to use our DOM helpers as well
3. `init-sidebar` will render the sidebar inside the app wrapper

Finally, we will create a `chat.core` namespace that will load the DOM elements for both the header and the sidebar into the page itself:

```clojure
(ns chat.core
    (:require [chat.message-bus :as bus]
              [chat.components.app :refer [init-app]]      ;; <1>
              [chat.handlers]                              ;; <2>
              [goog.dom :as gdom]))

(enable-console-print!)

(defonce initialized?
  (do
    (init-app                                              ;; <3>
      (gdom/getElement "app")
      bus/msg-ch)
    true))
```

_core.cljs_

1. Require the function that initializes the entire application UI
2. Require the `chat.handlers` function for the side effect of registering message handlers
3. Initialize the UI

We will continue to fill out this `chat.core` namespace as we continue with this project, but one thing to notice is that it will be less pure and functional than most of the rest of our code. This is because, at some point, we need to actually load things for side effects, call stateful functions, make assumptions about the DOM on the page, or load global objects, such as the `msg-ch` channel. Rather than polluting our entire code base with this impurity, we will do as many impure operations in the core namespace as possible in the interest of making the _interesting_ part of our code more modular.

One area in which we have not maintained functional purity in this application is in allowing UI components to access `chat.state/app-state` directly, rather than constructing it in `chat.core` and passing it down to each component explicitly. This is the sort of pragmatic trade-off that can sometimes be made to make the code easier to work with at the expense of testability and modularity. In a production application, we would usually be better served by a more constrained and explicit approach to accessing the state.

### Message List

Now that we have the basic "shell" of the application in place, let's move on to the meat of the application: the message list. Even though the message feed is the core of our application, it is implemented as a single component. The state accessor takes the `:messages` collection off of the application, does a simple lookup in the `:users` collection to get the author of each message, and applies some formatting. The renderer simply creates some DOM for each of these messages. Since there is nothing in the message list that needs to respond to user input, we do not have to deal with attaching any event handlers.

```clojure
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
  (dom/with-children el
    (map render-message messages)))

(defn scroll-to-bottom [el]                                ;; <1>
  (let [observer (js/MutationObserver.
                   #(set! (.-scrollTop el)
                          (.-scrollHeight el)))]
    (.observe observer el #js{"childList" true})))

(defn init-messages []
  (dom/section "messages"
    (doto (dom/div "messages-inner")
      (scroll-to-bottom)
      (init-component :messages accessor render))))
```

_components/messages.cljs_

1. We use a [Mutation Observer](https://developer.mozilla.org/en-US/docs/Web/API/MutationObserver) to set the viewport to the bottom of the message list any time the message list changes. This way, the user always sees the most recent messages.

Since the message list will be rendered as a top-level component, we will need to initialize it within `components/app.cljs`:

```clojure
(ns chat.components.app
  (:require ;; ...
            [chat.components.messages :refer [init-messages]]))

(defn init-main [msg-ch]
  (dom/section "content-main"
    ;; ...
    (init-messages)))
;; ...
```

With the message list done, let's move on to the message composer.

### Message Composer

The composer will be the simplest component that we have seen yet. It is simply a textarea with an event listener that will dispatch a message and clear its content whenever the user hits the `Enter`/`Return` key:

```clojure
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
```

_components/compose.cljs_

One thing to note before we move on is that we do not keep track of the message that the user is composing outside of the DOM itself. This means that in addition to our application state, we are relying on the DOM to hold some of our state. When we start building applications on top of React and Reagent, we will want to avoid this in favor of keeping all of our state in immutable data structures so that rendering is a simple, deterministic process of taking our app state and converting it to (virtual) DOM.

Like the message list, the composer will need to be mounted into the app and initialized within the main content area:

```clojure
(ns chat.components.app
  (:require ;; ...
            [chat.components.compose :refer [init-composer]]))

(defn init-main [msg-ch]
  (dom/section "content-main"
    ;; ...
    (init-composer)))
;; ...
```

### Authentication Modal

Since this is a multi-user chat application, we need to have some concept of users. We should then implement at least a simple sign up / sign in process so that users do not have to enter a first name, last name, and username every time they load the app. Additionally, we do not want users to impersonate each other. Below is the entire code listing for the authentication modal. We will walk through each section afterwards.

```clojure
(ns chat.components.auth
  (:require [chat.components.dom :as dom]
            [goog.dom.classes :as gdom-classes]
            [chat.components.component :refer [init-component]]
            [chat.components.render-helpers :as helpers]
            [chat.message-bus :as bus]
            [chat.state :as state]))

(declare accessor get-render sign-in-modal sign-up-modal
         auth-modal auth-form footer-link)

(defn init-auth [msg-ch]
  (init-component (dom/section "auth-modal")
    :auth
    accessor
    (get-render msg-ch)))

(defn accessor [state]
  (select-keys state [:current-user :auth-modal]))

(defn get-render [msg-ch]
  (fn [el {:keys [current-user auth-modal] :as s}]
    (if (some? current-user)
      (gdom-classes/add el "hidden")
      (doto el
        (gdom-classes/remove "hidden")
        (.appendChild
          (dom/div "auth-modal-wrapper"
            (if (= :sign-in auth-modal)
              (sign-in-modal msg-ch)
              (sign-up-modal msg-ch))))))))

(defn sign-in-modal [msg-ch]
  (auth-modal msg-ch
    {:header-text "Sign In"
     :footer-text "New here? Sign up."
     :form-fields [{:label "Username" :type "text" :name "username"}
                   {:label "Password" :type "password" :name "password"}]
     :submit-action :sign-in}))

(defn sign-up-modal [msg-ch]
  (auth-modal msg-ch
    {:header-text "Sign Up"
     :footer-text "Already have an account? Sign in."
     :form-fields [{:label "First Name" :type "text" :name "first-name"}
                   {:label "Last Name" :type "text" :name "last-name"}
                   {:label "Username" :type "text" :name "username"}
                   {:label "Password" :type "password" :name "password"}]
     :submit-action :sign-up}))

(defn auth-modal [msg-ch {:keys [header-text
                                 form-fields
                                 submit-action
                                 footer-text]}]
    (dom/div "auth-modal-inner"
      (dom/div "auth-modal-header"
        (dom/h1 nil header-text))
      (dom/div "auth-modal-body"
        (auth-form msg-ch form-fields submit-action))
      (dom/div "auth-modal-footer"
        (footer-link msg-ch footer-text))))

(defn auth-form [msg-ch form-fields submit-action]
  (let [form (dom/form nil
               (apply dom/with-children (dom/div)
                 (for [{:keys [label type name]} form-fields
                       :let [id (str "auth-field-" name)]]
                   (dom/div "input-field"
                     (dom/label #js {"class" "input-label"
                                     "for" id}
                       label)
                     (dom/input #js {"type" type
                                     "name" name
                                     "id" id}))))
               (dom/button #js {"type" "submit"} "Submit"))]
    (doto form
      (.addEventListener "submit"
        (fn [e]
          (.preventDefault e)
          (bus/dispatch! msg-ch submit-action
            (into {}
              (for [{:keys [name]} form-fields
                    :let [id (str "auth-field-" name)]]
                [(keyword name) (.-value (js/document.getElementById id))]))))))))

(defn footer-link [msg-ch footer-text]
  (doto (dom/a nil footer-text)
    (.addEventListener "click"
      (fn [e]
        (.preventDefault e)
        (bus/dispatch! msg-ch :toggle-auth-modal)))))
```

_components/auth.cljs_


Right at the top of the file, we run into a ClojureScript feature that we have not yet encountered: the `declare` macro. As the name suggests, this macro declares vars that are not bound to any value yet. This allows us to refer to functions and other values within the namespace before they are physically defined in the source of the file. In our case, we declare these vars for convenience so that we can list the high-level functions first and the functions that implement the lower-level details later in the code. This is not very idiomatic ClojureScript, but it is useful for the purpose of walking through the code.

Next, we define our component that will manage the auth modal using the `init-component` helper that we created earlier. In order to render the authentication modal, we need to know only 2 things about the application state: whether there is an authenticated user, and which state the modal (if displayed) should be in - sign in or sign up. Before returning a render function for this component, we perform the side effect of adding or removing a "hidden" class from the parent component.

Next, we define both the "Sign In" and "Sign Up" states of the modal. For each state, We need to know what to display in the header and footer, what form fields to display, and what message to dispatch when the form is submitted. Since there are just a few things that vary between each modal state, we factor out the common code to the `auth-modal` function, which `sign-in-modal` and `sign-up-modal` call with different data.

The `auth-modal` function in turn uses a couple of additional lower-level functions for its implementation. First, we have `auth-form`, which creates a `form` element with all of the input elements specified within `form-fields`. It then attaches a `submit` event handler that queries each of the elements in the form and wraps them into a map of the field name to the field value. It then emits the appropriate message type with the field/value map as the payload. Second, we have `footer-link`, which displays a toggle link to switch between the two states of the modal. Since the form and the footer link both need to emit messages that the application should respond to, we pass the `msg-ch` down the component hierarchy to each of these lower-level components.

Finally, as before, we need to initialize the modal in `components/app.cljs`:

```clojure
(ns chat.components.app
  (:require ;; ...
            [chat.components.auth :refer [init-auth]]))

(defn init-app [el msg-ch]
  (let [wrapper (dom/div "app-wrapper"
                  (init-auth msg-ch)
                  ;; ...
                )]
    ;; ...
  ))
```

With the UI complete, let's briefly look at how to hook up a WebSocket API.

## Realtime Communication

Since our application is highly dynamic, and we want to send and receive messages in near-realtime, we will use a WebSocket API. The code for this ClojureScript Node.js API is beyond the scope of this book to cover, but it is available within [the Learn ClojureScript GitHub repo](https://github.com/kendru/learn-cljs/tree/master/code/lesson-26/chat-backend). Since we already have the ability to handle messages within the application, and since we talk with the API in terms of messages, there is surprisingly little that needs to be done. The API should expose a function for sending messages to the API, and it should also emit messages from the API back to our application. One interesting thing to note is that we do not have the concept of a request/response flow, only asynchronous messages that flow within the UI as well as between the UI and the API:

```clojure
(ns chat.api
  (:require [chat.message-bus :as bus]
            [cljs.reader :refer [read-string]]))

(defonce api (atom nil))                                   ;; <1>

(defn send!
 ([msg-type] (send! msg-type nil))
 ([msg-type payload]
  (.send @api
    (pr-str (if (some? payload)                            ;; <2>
              [msg-type payload]
              [msg-type])))))

(defn init! [msg-ch url]
  (let [ws (js/WebSocket. url)]
    (.addEventListener ws "message"
      (fn [msg]
        (let [[type payload] (read-string (.-data msg))]   ;; <3>
           (bus/dispatch! msg-ch
             (keyword (str "api/" (name type)))            ;; <4>
             payload))))
    (reset! api ws)))
```

_api.cljs_

1. For convenience, we define the websocket API as a global atom
2. `pr-str` serializes Clojure(Script) data
3. `read-string` de-serializes Clojure(Script) data
4. Prefix all API events with `api/` to distinguish them from UI events

In this API namespace, we define an extremely simple messaging protocol. Both the UI and the API emit messages to each other that are serialized ClojureScript data structures. The first element is a keyword identifying the message type, and the second element can be an optional payload of arbitrary type. We use the `pr-str` and `read-string` functions in the standard library to serialize/deserialize data structures using the Clojure-native EDN format[^1].

We will first initialize the API within our core namespace:

```clojure
(ns chat.core
  (:require ;; ...
            [chat.api :as api]))

(defonce initialized?
  (do
    (api/init! bus/msg-ch js/WS_API_URL)                   ;; <1>
  ;; ..
  ))
```

1. Read a global WS_API_URL from the page to determine the API url. This can be set within a build script.

Next, we will update our `handlers` namespace to both emit API messages in response to certain UI messages as well as handle the messages that come directly from the API. First of all, we will send the API notifications when the user activates a specific room or conversation so that it can send message updates that are relevant for that specific location:

```clojure
(ns chat.handlers
  (:require ;; ...
            [chat.api :as api]))

;; ...

(bus/handle! bus/msg-bus :switch-to-conversation
  (fn [{:keys [username]}]
    (api/send! :set-view {:type :conversation, :username username})
    ;; ...
  ))

(bus/handle! bus/msg-bus :switch-to-room
  (fn [{:keys [id]}]
    (api/send! :set-view {:type :room, :id id})
    ;; ...
  ))
```

Naturally, we will want to notify the server when we have sent a message. Since the server keeps track of what room or conversation we are currently in, all we need to send is the message itself.

```clojure
(bus/handle! bus/msg-bus :add-message
  (fn [content]
    (api/send! :add-message {:content content})))
```

Since we let users create new rooms, we also need to send the server a message when this occurs.

```clojure
(bus/handle! bus/msg-bus :create-room
  (fn [name]
    (api/send! :create-room {:name name})))
```

The auth modal emits events that should be used to sign in or sign up, so let's send those to the server as well.

```clojure
(bus/handle! bus/msg-bus :sign-in
  (fn [data]
    (api/send! :sign-in data)))

(bus/handle! bus/msg-bus :sign-up
  (fn [data]
    (api/send! :sign-up data)))
```

That takes care of all of the messages that need to be sent _to_ the API based on user interaction. Now, let's handle the messages from the API to which the UI needs to react. First, let's handle authentication. Whenever a user successfully logs in or signs up, the API will send an `authenticated` message with user details. We should set this as the current user in the application state then request user and room lists to populate the sidebar:

```clojure
(bus/handle! bus/msg-bus :api/authenticated
  (fn [user-info]
    (swap! state/app-state state/user-authenticated user-info)
    (api/send! :list-people)
    (api/send! :list-rooms)))
```

Once we have received the rooms list from the server, we should set the rooms list in the application state and switch to the first room in the list as the initial room.

```clojure
(bus/handle! bus/msg-bus :api/rooms-listed
  (fn [rooms]
    (swap! state/app-state state/received-rooms-list rooms)
    (when-let [first-room (first rooms)]
      (bus/dispatch! bus/msg-ch :switch-to-room
        {:id (:id first-room)}))))
```

Next, let's handle the messages for when we receive the list of users, when a user joins, and when a user leaves. These handlers are simple because they simply pass the data from the API through to a state transition function that we write earlier.

```clojure
(bus/handle! bus/msg-bus :api/people-listed
  (fn [people]
    (swap! state/app-state state/received-people-list people)))

(bus/handle! bus/msg-bus :api/person-joined
  (fn [person]
    (swap! state/app-state state/person-joined person)))

(bus/handle! bus/msg-bus :api/person-left
  (fn [username]
    (swap! state/app-state state/person-left username)))
```

Next, we will write handlers for receiving a single message as well as a list of messages - which will occur when the user switches rooms or conversations. Also, we will create a `should-set-message?` function that we can use to determine whether the message or messages from the API are still relevant for display. This will prevent us from accidentally posting a message from the previous room when the user switches to a new room before the API is aware of the switch.

```clojure
(defn should-set-message? [username room]
  (let [app @state/app-state]
    (or
      (and (some? username)
           (state/is-current-view-conversation? app)
           (= username (state/current-conversation-recipient app)))
      (and (some? room)
           (state/is-current-view-room? app)
           (= room (state/current-room-id app))))))

(bus/handle! bus/msg-bus :api/message-received
  (fn [{:keys [message username room]}]
    (when (should-set-message? username room)
      (swap! state/app-state state/message-received message))))

(bus/handle! bus/msg-bus :api/messages-received
  (fn [{:keys [messages username room]}]
    (when (should-set-message? username room)
      (swap! state/app-state state/messages-received messages))))
```

Finally, we need one more handler for populating a new room on creation.

```clojure
(bus/handle! bus/msg-bus :api/room-created
  (fn [room]
    (swap! state/app-state
      #(-> %
           (state/room-added room)
           (state/create-room-input-closed)))))
```

With that handler complete, we have a fully functional chat application!

### Challenge

While this application is quite capable considering how few lines of code it contains, there are still many improvements that could be made. Try one or two of the following:

- Only render a certain number of messages at a time, rendering more only when the user scrolls to the point where they will be needed
- Display error messages from the server. Errors will follow the format: `[:error {:message "Some message"}]`
- Automatically reconnect the WebSocket if it closes

## Summary

If you have reached this point, congratulations on creating a non-trivial application in ClojureScript! At this point, we have learned all of the core language features and idioms, and we have put them to practice in creating an interesting, useable chat app. While we ended up with a fully-functional app, we had to resort to some imperative code and manual DOM manipulation, similar to what we would do in JavaScript if we were not using a framework. In the next section, we will see how React's virtual DOM and Clojure Script's preference for immutability form a perfect marriage that will allow us to write declarative application UIs.

[^1]: https://github.com/edn-format/edn
