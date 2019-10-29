---
title: "Capstone 3 - Contact Book | Lesson 20"
date: 2019-10-06T17:19:19-06:00
type: "docs"
draft: true
---

# Lesson 20: Capstone 3 - Contact Book

Over the past few lessons, we have learned the core tools for working with data in
ClojureScript. First, we learned about the basic collection types - lists, vectors,
maps, and sets - as well as the most common functions for working with these collection
types. Then we took a closer look at the important _sequence abstraction_ that allows
us to operate with all sorts of sequential data with a uniform interface. Next, we
discovered the `reduce` function and the many cases in which it can be applied to
summarize a sequence of data. Last, we walked through the process of modeling a real-
world domain. With this knowledge in our possession, we are ready to build another
capstone project. This time, we will take the example of a contact book that we
mentioned back in Lesson 16, and we will build a complete contact book implementation,
similar to what is found in most email clients.

---

*In This Lesson:*

- Create a complete ClojureScript application without any frameworks
- ...

---

## Data Modeling

In this lesson, we will use the techniques and patterns from the previous chapter to
model the data for our contact book. The goal will be to practice what we have learned
rather than to introduce any new material. We will primarily model our data using maps
and vectors, and we will implement the constructor function pattern for creating new
contacts. We will also implement the operations that the UI will need to update the
contact list using simple functions to transform our data. With that, let's dig in to
the data model.

### Constructing Entities

Since a contact book at its core represents an ordered list of contacts, we will need
a data structure to represent that ordered list, and as we have already seen, a vector
fits the bill nicely. We can define an empty contact list simply as an empty vector -
no constructor function necessary:

```clojure
(def contact-list [])
```

Since an empty list is not all that interesting, let's think about the contacts that
this will hold. Each contact will need a first name, last name, email address, phone
number, and a physical address, including city, state, and country. This can easily
be accommodated with a nested map, such as the following:

```clojure
{:first-name "Phillip"
 :last-name "Jordan"
 :email "phil.j@hotmail.com"
 :phone "111-222-3456"
 :address {:street "523 Sunny Hills Cir."
           :city "Springfield"
           :state "MI"
           :country "USA"}}
```

In order to construct a new contact, we will use a variation on the constructor pattern
introduced in the last lesson. Instead of passing in each field individually, we will
pass in a map that we expect to contain zero or more of the fields that make up a
contact. You will recall from the last lesson that the `select-keys` function takes
a map and a collection of keys that should be selected, and it returns a new map with
only the selected keys copied from the original map. We can use this function to
sanitize the input and ensure that our contact contains only valid keys.

```clojure
(defn make-contact [contact]
  (select-keys contact [:first-name :last-name :email :phone :address]))
```

Since address itself is a map, let's factor out creation of an address to another
function that also selects the valid address keys. We can then update the
`make-contact` function to use this address constructor:

```clojure
(defn make-address [address]
  (select-keys address [:street :city :state :country]))

(defn make-contact [contact]
  (let [clean-contact (select-keys contact [:first-name :last-name :email :phone])]
    (if-let [address (:address contact)]
      (assoc clean-contact :address (make-address address))
      clean-contact)))
```

This new version of `make-contact introduces` one expression that we have not seen before:
`if-let`. This macro works just like `if` except that it binds a name to the value being
tested (just like `let` does). Unlike `let`, only a single binding may be provides. At compile
time, this code will expand to something like the following[^1]:

```clojure
(if (:address contact)
  (let [address (:address contact)]
    (assoc clean-contact :address (make-address address)))
  clean-contact)
```

_`if-let` Transformation_

However, we can make the `make-contact` function a bit more concise and easier to read
using one of ClojureScript's _threading macros_, `->` (pronounced "thread first"). This
macro allows us to take what would otherwise be a deeply nested expression and write it
more sequentially. It takes a value and any number of function calls and injects the value
as the first argument to each of these function calls. Seeing this transformation in
action should make its functionality more intuitive:

```clojure
(-> val                                                    ;; <1>
    (fn-1 :foo)                                            ;; <2>
    (fn-2 :bar :baz)                                       ;; <3>
    (fn-3))

(fn-3                                                      ;; <4>
  (fn-2
    (fn-1 val :foo)
    :bar :baz))
```

_Thread-First Transformation_

1. Start with `val` as the value to thread through the following expressions
2. `fn-1` will be evaluated with the arguments, `val` and `:foo`
3. The result of the evaluation of `fn-1` will be threaded as the first argument to `fn-2`
4. The macro will rewrite into a nested expression that evaluates `fn-1` then `fn-2`, then `fn-3`

This macro is extremely common in ClojureScript code because of how it enhances the readability
of our code. We can write code that looks sequential but is evaluated "inside-out". There are
several additional threading macros in ClojureScript that we will not go into now, but we will
explain their usage as we run into them. With this macro, we can make our `make-contact` function
even clearer:

```clojure
(defn maybe-set-address [contact]                          ;; <1>
  (if (:address contact)
    (update contact :address make-address)
    contact))

(defn make-contact [contact]
  (-> contact                                              ;; <2>
      (select-keys [:first-name :last-name :email :phone])
      (maybe-set-address)))
```

1. Refactor the code that conditionally constructs an address
2. Rewrite `make-contact` using the `->` macro

<!-- TODO: Insert diagram of threading macros -->

### Quick Review

- Does `if-let` allow multiple bindings? For example, what would this code do?

```clojure
(if-let [contact (find-by-id 123)
         address (:address contact)]
  (println "Address:" (format-address address)))
```

- How would the `->` macro rewrite the following expression?

```clojure
(let [input {:password "s3cr3t"}]
  (-> input
      (assoc :password-digest (-> input :password digest))
      (dissoc :password)))
```

### Defining State Transitions

In order for our UI to do anything other than display a static list of contacts
that we define in code, we need to create function for the interactions that we want
the UI to be able to perform. Again, we are building our low-level domain logic
before any UI code so that we can take advantage of the bottom-up programming style
that ClojureScript encourages - composing small, granular functions into larger and
more useful structures.

First, we will want the user to be able to add a new contact to the contact list. We
can assume that we will receive some sort of form data as input, which we will pass
to our `make-contact` constructor, adding the resulting contact to our contact list.
We will need to pass in the contact list and input data as arguments and produce a new
contact list.

```clojure
(defn add-contact [contact-list input]
  (conj contact-list
        (make-contact input)))
```

We can paste these function definitions into the REPL and then test them to make sure that
they function as expected:

```clojure
cljs.user=> (-> contact-list                               ;; <1>
                (add-contact {:email "me@example.com"
                              :first-name "Me"
                              :address {:state "RI"}})
                (add-contact {:email "you@example.com"
                              :first-name "You"}))
[{:first-name "Me", :email "me@example.com"}
 {:first-name "You", :email "you@example.com"}]
```

_Testing with the REPL_

1. Once again, the `->` macro makes our code easier to read and write

Next, we will need a way to remove a contact from the list. Since we are using a vector to
hold our contacts, we can simply remove an element at a specific index:

```clojure
(defn remove-contact [contact-list idx]
  (vec                                                     ;; <1>
    (concat                                                ;; <2>
      (subvec contact-list 0 idx)                          ;; <3>
      (subvec contact-list (inc idx)))))
```

_Removing a Contact_

1. `vec` converts a sequence into a vector
2. `concat` returns a `seq` that contains all elements in the sequences passed to it in order
3. `subvec` returns a portion of the vector that it is given

Since there are a couple of new functions here that we have not seen yet, let's quickly
look at what they do. Starting from the "inside" of this function, we have `subvec`. This
function provides an efficient way to obtain a slice of some vector. It comes in a 2-arity
and a 3-arity form: `(subvec v start)` and `(subvec v start end)`. This function work similarly
to JavaScript's `Array.prototype.slice()` function. It returns a new vector that starts at
the `start` index of the original vector and contains all elements up to but not including the
`end` index. If no `end` is provided, it will contain everything from `start` to the end of the
original vector.

Next, we have `concat`. This function takes a number of sequences and creates a new lazy[^2]
`seq` that is the concatenation of all of the elements of its arguments. Because the result
is a `seq`, we use the `vec` function to coerce the result back into a vector. Since much of
ClojureScript's standard library operates on the sequence abstraction, we will find that we
often need to convert the result back into a more specific type of collection.

Now that we can add and remove contacts from the contact list, we should have a way to update
a specific contact. We want to be able to provide the index of a contact to update and a
transformation function to be applied to that contact. Fortunately, this is exactly
what the built-in `update` function does:

```clojure
(update
  contact-list                                             ;; <1>
  7                                                        ;; <2>
  (fn [contact] (assoc contact :name "Ted")))              ;; <3>
```

_Updating a Contact_

1. Collection to update
2. Key of element in collection to update
3. Transformation function to be applied to element

### Quick Review

- We mentioned that `vec` converts a sequence into a vector. Given what we learned about the sequence abstraction, what will happen if you pass `vec` a map? What about a set?

## Creating the UI

Now that we have defined all of the functions that we need to work with our application state,
let's turn our attention to creating the application UI. In Section 5, we will learn how to
create performant UIs using the Reagent framework, but for now, we will take naive approach of
re-rendering the entire application whenever anything changes. Our application will have two
main views - a list of contacts that displays summary details about each contact and a larger
pane for viewing/editing contact details.

<!-- TODO: Insert screenshot -->

We will use the [hiccups](https://github.com/teropa/hiccups)
library to transform plain ClojureScript data structures into an HTML string. This allows us to
represent the interface of our application as a ClojureScript data structure and have a very simple
interface to the actual DOM of the page. In order to use this library, we need to add it to our
dependencies in `project.clj`:

```clojure
:dependencies [;; ...Other dependencies
               [hiccups "0.3.0"]]
```

_`project.clj`_

Then, we need to import this library into our `core` namespace. Note that since we are using
a macro from this library, the syntax is a little different:

```cloure
(ns contacts.core
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime]))
```

_`core.cljs`_

The translation between ClojureScript data structures and HTML is very simple:

1. HTML tags are represented by vectors whose first element is the tag name as a keyword
2. Attributes are represented by maps and should come immediately after the tag name. Omitting attributes is fine.
3. Any remaining elements in the vector are children of the outer element

For example, the following code renders a div containing a single anchor tag:

```clojure
(hiccups/html                                              ;; <1>
  [:div                                                    ;; <2>
    [:a {:href "https://www.google.com"                    ;; <3>
         :class "external-link"}
        "Google"]])                                        ;; <4>
;; <div><a class="external-link" href="https://www.google.com">Google</a></div>
```

_Rendering With Hiccups_

1. The `html` macro renders hiccups data to an HTML string
2. Create a `div`. We do not need to specify any attributes
3. Create an `a` element with an attributes map as a child of the outer `div`
4. The `a` only contains text

With this knowledge, we can start defining components for our various UI elements
that produce a hiccups-compatible data structure. We can compose these functions
together to create a data structure that represents the entire UI, and we'll
pass this to another function that renders the whole structure to the page.
Let's start with the component that displays the contact summary in the list. For
this app, we will be using the Bulma CSS framework to help with styling, so most
of the markup that we are generating is for the purpose of styling the page.

```clojure
(defn format-name [contact]                                ;; <1>
  (->> contact                                             ;; <2>
       ((juxt :first-name :last-name))                     ;; <3>
       (str/join " ")))

(defn delete-icon [idx]
  [:span {:class "delete-icon"
          :data-idx idx}
    [:span {:class "mu mu-delete"}]])

(defn render-contact-list-item [idx contact selected?]
  [:div {:class (str "card contact-summary" (when selected? " selected"))
         :data-idx idx}                                    ;; <4>
    [:div {:class "card-content"}
      [:div {:class "level"}
        [:div {:class "level-left"}
          [:div {:class "level-item"}
            (delete-icon idx)
            (format-name contact)]]
        [:div {:class "level-right"}
          [:span {:class "mu mu-right"}]]]]])
```

_Rendering a Contact Summary_

1. Extract the logic for a contact's display name into another function
2. Use the `->>` (thread last) macro to pass a value through as the last argument to each subsequent function
3. The `juxt` function extracts the first and last name from the contact. It is described below
4. `idx` is needed so that we will be able to get the correct contact in our event handlers

Both of the things that we should note about this code occur in the `format-name` function.
First, we use the `->>` macro to thread a value through several function calls. This works
almost exactly like the `->` macro that we used earlier in this lesson with the exception that
it feeds threads the value as the _last_ argument to each subsequent function. Second, the
`juxt` function is a very interesting function that deserves a bit of an explanation.

Now that we can render a single contact list item, let's create the list that will display
each list item:

<!-- Create functions for rendering each component:
Contact details
Contact list item -->

<!-- Describe composition via functions. -->

<!-- Create Contact List view render. -->

<!-- Explain ease of introducing new UI components, and change phone # to phone #s (type/num) list -->

## Implementing Undo/Redo

<!-- Explain that immutable data = ability to keep state at any point in time - no
per-operation logic for determining how to undo. -->

## Summary

While this application is not a shining example of modern web development (don't worry -
we will get to that in Section 5), it showed us how to build a data-driven application
using the techniques that we have been learning in this section.

[^1]: The actual implementation of the `if-let` macro is slightly more complex, but the effect is the same.
[^2]: Lazy evaluation was covered in Lesson 11.