---
title: "Capstone 5 Agile Estimates | Lesson 30"
date: 2020-06-27T11:59:16-06:00
type: "docs"
---

# Lesson 30: Capstone 5 - Notes

In this section, we have been learning how to use the Reagent framework to apply our ClojureScript knowledge to web applications. In this final capstone lesson, we will once again use a project to synthesize what we have learned about Reagent and modular application design. As in the previous capstone lessons, this one will draw on all that we have learned so far - from working with sequences to state management and asynchronous communication. At this end of this lesson, we will have created a note-taking application from scratch. As was the case in the previous capstone, we will only be building the front-end. In order to follow along, you can use the API running at `https://notes.learn-cljs.com/api` with a set of credentials that can be obtained by issuing a `POST` request to `https://notes.learn-cljs.com/api/accounts`.

---

_In This Lesson:_

- Create a flexible component-based UI
- Handle state management with Reagent
- Interact with a RESTful API

---

## What We Are Building

The motivation for this capstone came from the author's own desire to have a simple note-taking app that could easily be modified and extended as his needs changed. The core functionality of this app is that a user can take notes, classify them via tags, and search across notes that have already been added. The API for this application is deliberately uninteresting for two reasons:

1. It is designed to look like most of the APIs that we as web developers interact with at our jobs.
2. The primary focus of this lesson is on building UIs, so the less noise introduced by the API, the better.

In addition to performing CRUD actions for notes, users should also be able to tag notes, navigate to related notes based on a tag, and filter notes by tag. Finally, users should be able to use search to view both notes and tags that match their query. This app is designed to be used by a single user and does not require any authentication or authorization.

## State Management

Now that we know what we are building, it is time to start modeling the data and coming up with the patterns that the UI components will use to access that data.

We will start with a basic model for the UI state. We will need to keep track of notes, tags, and the relationships between them. Since we will be getting the data from a server, we will need to consider its data model when deciding how to store and access that data from the UI. We will be making use of two primary endpoints: `/notes` to list all notes, and `/tags` to list all tags. However, as is the case in most real-world apps, the data will not be in the ideal format for consumption by the UI, so we will need to reshape it with a process called normalization.

### Data Normalization

One of the main ideas that relational database technology has brought us is the concept of normalization. While normalization does have a technical definition, we only need to be concerned with an informal description of the idea: in the canonical application state, data should be shared via reference rather than duplication. Applying this idea to our app leads us to store notes and tags separately and to maintain a list of the links between them. Additionally, we want to structure them in a way that makes lookups efficient. For example, imagine we retrieve the following notes from the API.

```clojure
[{:id 1
  :title "Books to Read"
  :content "..."
  :tags [{:id 2 :name "list"}
         {:id 3 :name "reading"}]}
 {:id 2
  :title "Groceries"
  :content "..."
  :tags [{:id 1 :name "food"}
         {:id 2 :name "list"}]}]
```

The first difficulty with this data structure is that the tags are nested under each note. For note views this is fine, but if we are viewing or editing tags, this structure is less than ideal. If we maintained a separate collection of tags, then we would have to worry about applying any change that we make to a tag to the copy of that tag that is nested under the notes as well. The solution here is to do the same thing that we would do if we had a many-to-many relationship in a relational database management system: create separate collections for notes, tags, and the relationships between them. The goal is to transform the data into a shape like the following:

```clojure
{:notes
  {:by-id                                                  ;; <1>
    {1 {:id 1
        :title "Books to Read"
        :content "..."}
     2 {:id 2
        :title "Groceries"
        :content "..."}}}
 :tags
   {:by-id
     {1 {:id 1 :name "food"}
      2 {:id 2 :name "list"}
      3 {:id 3 :name "reading"}}}

 :notes-tags
   {:by-note-id                                            ;; <2>
     {1 [2 3]
      2 [1 2]}
    :by-tag-id
     {1 [2]
      2 [1 2]
      3 [1]}}}
```

1. Each entity is stored in a map indexed by its id for easy retrieval.
2. References are stored in a separate map for each direction (`note -> tags` and `tag -> notes`) for easy lookup.

The astute reader may see that this code does not completely live up to the promise of avoiding duplication, since each reference is effectively stored twice. In practice, however, this duplication is not a problem, and adding/removing tags from notes is still a simple operation.

To re-shape this data, we need to create several indexes. The purpose of these indexes is to quickly look up any note or tag, and given any note, look up its corresponding tags (as well as the reverse tag to notes lookup). In the case of tags and notes, we simply want to form a map where the id is associated with the resource that has that id. Since each id is unique, there will only be one resource for any given id. The `group-by` function gives us almost what we want but not _quite_:

```clojure
cljs.user=> (def items [{:id 1 :title "foo"}
                        {:id 2 :title "bar"}])
#'cljs.user/items

cljs.user=> (group-by :id items)
 {1 [{:id 1, :title "foo"}],
  2 [{:id 2, :title "bar"}]}
```

The `group-by` function takes a group function `f` and a collection `xs`, and it returns a map of `(f x)` to a vector of all items that yielded the same `(f x)`. A keyword is commonly used as the group function so that all items with the same keyword property are grouped together. Since we know that all ids will have only one element in their group, we can take the first element from every map value. The ClojureScript library does not come with a function for transforming every value in a map, but we can write one trivially:

```clojure
(defn map-values [f m]
  (into {} (for [[k v] m] [k (f v)])))
```

This function uses a `for` sequence comprehension to iterate over every entry in `m`, yielding another entry that has the same key but a value that has had `f` applied, collecting all of these entries into a new map. We can now use this to write a new indexing function:

```clojure
cljs.user=> (defn make-index [f coll]
              (->> coll
                   (group-by f)
                   (map-values first)))
#'cljs.user/make-index

cljs.user=> (let [items [{:id 1 :title "foo"}
                         {:id 2 :title "bar"}]]
              (make-index :id items))
{1 {:id 1, :title "foo"},
 2 {:id 2, :title "bar"}}
```

This function will work for the primary note and tag indexes, but we need a slightly different strategy for handling the `:notes-tags` indexes. For one thing, these will not be unique indexes, so we will want to have multiple elements in each group. For another thing, we are only concerned with the id to id mapping, so we do not need full note or tag objects in these indexes. That means that we will want to be able to map over the elements in each group and extract a single property from each one. Consider the following:

```clojure
cljs.user=> (def links [{:note-id 1 :tag-id 2}
                        {:note-id 1 :tag-id 3}
                        {:note-id 2 :tag-id 1}
                        {:note-id 2 :tag-id 2}])
#'cljs.user/links

cljs.user=> (group-by :note-id links)
{1 [{:note-id 1, :tag-id 2} {:note-id 1, :tag-id 3}],
 2 [{:note-id 2, :tag-id 1} {:note-id 2, :tag-id 2}]}
```

Once again, `group-by` gives us almost what we want. Instead of applying a function to each group, we need to apply a function to each item within the group. This is slightly more complicated, but it still only requires the same few familiar sequence functions that we are used to working with:

```clojure
cljs.user=> (->> links
                 (group-by :note-id)
                 (map-values #(mapv :tag-id %)))
{1 [2 3],
 2 [1 2]}
```

We can modify the `make-index` function so that it handles both of the cases that we need if we allow it to take an optional function that transforms each group as well as an optional function that transforms each element in the group. One way to handle optional arguments is the "kwargs" (keyword args) pattern. A function's parameter vector can end with an `&` followed by a map destructuring pattern. The function will then accept zero or more pairs of arguments that are a keyword and a value associated with it. We can now write our final `make-index` function:

```clojure
(defn make-index [coll & {:keys [index-fn value-fn group-fn]
                          :or {value-fn identity
                               group-fn identity}}]
  (->> coll
       (group-by index-fn)
       (map-values #(group-fn (mapv value-fn %)))))

;; Example usage:
cljs.user=> (make-index items
                        :index-fn :id
                        :group-fn first)
{1 {:id 1, :title "foo"},
 2 {:id 2, :title "bar"}}
cljs.user=> (make-index links
                        :index-fn :note-id
                        :value-fn :tag-id)
{1 [2 3],
 2 [1 2]}
```

With this function written, we need only to write a function to extract all note-id/link-id pairs and a final response normalization function.

```clojure
(defn get-links [notes]
  (mapcat (fn [note]
            (for [tag (:tags note)]
              {:note-id (:id note)
               :tag-id (:id tag)}))
          notes))

(defn normalize-response [notes]
  (let [links (get-links notes)
        notes-without-tags (mapv #(dissoc % :tags) notes)
        all-note-tags (mapcat :tags notes)]
    {:notes {:by-id (make-index notes-without-tags
                                :index-fn :id
                                :group-fn first)}
     :tags {:by-id (make-index all-note-tags
                               :index-fn :id
                               :group-fn first)}
     :notes-tags
     {:by-note-id
      (make-index links
                  :index-fn :note-id
                  :value-fn :tag-id)
      :by-tag-id
      (make-index links
                  :index-fn :tag-id
                  :value-fn :note-id)}}))
```

Now that the data normalization is working as we expect, it is time to move on to the architecture that we will use for state management and coordination.

<!-- TODO: Add Quick Check or challenge -->

### Additional UI State

In addition to the data that we retrieve from the server, there are a few more pieces of state that we will maintain:

```clojure
(def initial-state
  {:current-route [:home]                                  ;; <1>
   :search-input ""                                        ;; <2>
   :notifications {:messages []                            ;; <3>
                   :next-id 0}
   :data {:notes {:order-by :created-at                    ;; <4>
                  :order-dir :desc}
          :tags {:order-by :name
                 :order-dir :asc}}})

(defonce app (r/atom initial-state))
```

_state.cljs_

1. Route parameters for the current route. The state will serve as the source of truth for routing, and we will be using a routing library to keep the URL in sync with the state.
2. The input for the search box.
3. Notifications for display using a component adapted from [Lesson 29](/section-5/lesson-29-separate-concerns).
4. Some additional state to help us appropriately sort the data from the server.

This minimal amount of state is all that we need to build the capstone project, so let's move on to the architecture that we will use to coordinate updates to state.

### Coordination Architecture

The overall architecture that we will be using follows the _command/event_ pattern from Lesson 29. The flow will be as follows:

1. The UI will issue commands by calling a `notes.command/dispatch!` function directly.
2. A command handler will perform any side effects needed for the command (including calling an API) and emit zero or more events to an event bus.
3. State update functions will listen for events and update the global application state accordingly.

Another departure from Lesson 29 is that we will not be using `core.async` for the messaging. While `core.async` would work here perfectly well, it is a bit overkill for the simple case where we have one function that emits events and one place where we dispatch to event handlers.

First up, we need a command dispatcher. This is a simple function that takes a command name and an optional command payload, and it will dispatch to some function that will perform side effects and emit events:

```clojure
(ns notes.command
  (:require [notes.events :refer [emit!]]))

(defn handle-test-hello! [name]
  (println "Hello" name)                                   ;; <1>
  (emit! :test/greeting-dispatched {:name name}))          ;; <2>

(defn dispatch!
  ([command] (dispatch! command nil))
  ([command payload]
   (case command
     :test/hello (handle-test-hello! payload)

     (js/console.error (str "Error: unhandled command: " command)))))
```

_command.cljs_

1. The handler function may perform side effects.
2. It should also emit events to which other portions of the app can react.

The UI will then issue commands by calling the `notes.command/dispatch!` function directly. For example, a component could call `(notes.command/dispatch! :test/hello "world")`, which would print `Hello world` to the console. To support more commands, we will add a new case to the `case` expression in the `dispatch!` function and a corresponding handler function.

Next, we need to implement the `emit!` function that will be responsible for delivering events to subscribers. We will allow anyone to register a listener function that will be called whenever an event is emitted so that it can have a chance to react to it.

```clojure
(ns notes.events)

(def listeners (atom []))                                  ;; <1>

(defn emit!                                                ;; <2>
  ([type] (emit! type nil))
  ([type payload]
   (doseq [listen-fn @listeners]
     (listen-fn type payload))))

(defn register-listener! [listen-fn]                       ;; <3>
  (swap! listeners conj listen-fn))
```

_events.cljs_

1. Keep track of the listener functions to notify when an event is emitted.
2. Call each listener function in succession with the event type and payload.
3. Allow other code to register a listener.

Note that when we declare listeners, we use `def` rather than `defonce`. This is intentional and will allow us to re-register listeners every time the app is reloaded, which means that when we update event handler functions, we do not need to perform a full reload of the app to see the change.

Finally, we will register a listener that will be responsible for performing any necessary updates to the app state when an event occurs.

```clojure
(ns notes.state
  (:require [reagent.core :as r]
            [notes.events :as events]))

;; ...

(def handlers (atom {}))

(defn register-handler! [event-type handler-fn]
  (swap! handlers assoc event-type handler-fn))

(events/register-listener!
 (fn [type payload]
   (when-let [handler-fn (get @handlers type)]
     (swap! app #(handler-fn  % payload)))))
```

_state.cljs_

Now, from anywhere in the code, we can register a handler that will update the app state whenever an event occurs. That handler will be passed the state of the database and the event payload, and it is expected to return a possibly updated state for the database.

You may notice that we created the event bus in such a way that may listeners could be registered, but we only register a listener for state updates. Why the extra layer of indirection rather than have the command dispatcher update the app state directly? The main reason is that there is now one place to tap into if we wanted to log events, save them in memory or `localStorage` to send to a server in an automated bug report, or integrate with a third-part component that is not aware of our state structure. Decoupling the act of emitting an event from updating the app state buys us a lot of flexibility in the long run for very little effort up-front.

To recap to flow of our state management:

1. A UI component dispatches a command using `command/dispatch!`.
2. The command dispatcher invokes a handler function, which can emit events and may also perform side effects, such as making API calls.
3. The event bus emits the event to listeners.
4. The state listener handles the event by passing the event and the current state of the database to any handlers registered for that event.
5. An event handler will take the event and the current state of the database and will return an updated state.
6. The update state will propagate to any components that depend on it, and they will re-render.

## Building the Application

In the first part of this lesson, we focused on a "horizontal slice" of functionality - state management. Since state management is such a core concern to any front-end app, it is important that it be well-designed. However, we will now turn to a "vertical slices" approach to building the rest of the application. That is, we will focus on one piece of functionality at a time and develop the UI components, state handlers, API functions, etc. The first piece that we will look at now is the layout.

<!-- TODO: Layout wireframe -->

The layout will be fairly simple, with a header containing a search box, a sidebar with a list of notes, and a main content area where we can view and update notes. We will add most of this structure in our top-level `core.cljs` file:

```clojure
(ns notes.core
  (:require [notes.ui.header :refer [header]]
            [notes.ui.main :refer [main]]
            [notes.ui.sidebar :refer [sidebar]]
            [notes.ui.footer :refer [footer]]
            [reagent.dom :as rdom]
            [goog.dom :as gdom]))

(defn app []
  [:div.app
   [header]
   [main]
   [sidebar]
   [footer]])

(rdom/render
 [app]
 (gdom/getElement "app"))
```

_core.cljs_

We have not created the `header`, `main`, `sidebar`, or `footer` components yet, so let's do that now, starting with the header.

```clojure
(ns notes.ui.header)

(defn header []
  [:header.page-header])
```

_ui/header.cljs_

The main file will be a similar skeleton for now:

```clojure
(ns notes.ui.main)

(defn main []
  [:div.main])
```

_ui/main.cljs_

We will then follow the same pattern for the sidebar:

```clojure
(ns notes.ui.sidebar)

(defn sidebar []
  [:nav.sidebar])
```

_ui/sidebar.cljs_

Next, we will create the footer, which will simply display the name of the application. Since this is a static layout component, we will not need to revisit it for the rest of the lesson.

```clojure
(ns notes.ui.footer)

(defn footer []
  [:footer.footer "CLJS Notes"])
```

_ui/footer.cljs_

- Derived state
  - Normalize data
  - Join via reactions
  - Filter via reactions
- Mention re-frame - strong recommendation that it is a good way to structure apps.
  - Difference is that _everything_ is an event (no commands), and it separates side effects from app state (db) updates.
  - Keeps logic for both state changes and effects centralized
- API
  - CRUD actions for notes - list can include tag for filter
  - Pagination?
  - Search - term/results (may be note or tag w/ note count)
- Navigation
  - Use router as integration point between URL and state.
  - Keep state as the source of truth - state changes drive routing as a side effect
  - Declare routes, helper for testing current route, write state transition function, initialize router
  1. Core imports routes
  2. Routes initializes bide router once, configuring to emit a `:route/navigated` event whenever route changes. This will fire when the browser's navigation causes the route to change. The purpose is to keep the state in sync with the browser. The browser is essentially the one emitting this event.
  3. Command exposes a `:route/navigate!` command, which calls the `routes/navigate!` function. This function instructs the router to navigate and relies on the bide handler to emit the correct event on navigation.
  4. We create and register a `notes.event-handlers.routes` namespace that registers a state update handler for the `:route/navigated` event that updates the app state appropriately.
- UI
  - Layout (TODO: Add event handler import when it makes sense)
  - Navigation (Add routes require to core.cljs)
  - List and View/Edit for notes
  - Inline tag creation
  - Search
    - Multiple components for results (notes vs. tags)
- Conclusion
  - We have created a well-structured UI application.
  - This is not just a toy project.
  - Tied together everything from state management to Reagent to async programming.
  - Thanks for joining
