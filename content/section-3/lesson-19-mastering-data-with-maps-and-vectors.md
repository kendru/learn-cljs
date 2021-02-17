---
title: "Mastering Data With Maps and Vectors | Lesson 19"
date: 2019-10-22T15:00:46+03:00
type: "docs"
---

# Lesson 19: Mastering Data With Maps and Vectors

In this lesson, we will explore some of the features of ClojureScript that make it simple to work with data. ClojureScript places a strong emphasis on relying on generic collection types and the standard functions that operate on them rather than creating highly specialized functions that only work on a single type of object. The object-oriented approach, which most mainstream languages encourage, is to create objects that encapsulate both the data and behavior of a specific type of "thing". The practice that ClojureScript encourages, however, is to separate functions and data. Data is pure information, and functions are pure transformations of data.

---

In this lesson:

- Master the most common map functions: `assoc`, `dissoc`, `merge`, and `select-keys`
- Get and set deeply-nested values
- Use the constructor pattern for creating common objects

---

![Functions and Data](/img/lesson19/functions-and-data.png)

_Functions and Data_

### Example: Modeling a Domain

Say that we have been tasked with creating an analytics app. Before we get started, we want to model the type of objects that we will be working with. If we were using a statically typed language, we would probably start by writing type definitions. Even if we were working in JavaScript, we would likely define "classes" for the objects that we will be working with. As we define these objects, we would have to think about both the data that they contain and the operations that they support. For example, if we have a `User` and a `ClickEvent`, we might need the operation, `User.prototype.clickEvent()`.

![Our analytics domain deals users and their actions](/img/lesson19/analytics-domain.png)

_Our analytics domain deals users and their actions_

With ClojureScript, we will consider data and functions separately. This approach ends up being flexible, as we will see that most of the operations that we want to perform on the data are simple and re-usable. In fact, it is common to find that the exact operation that you need is already part of the standard library. Ultimately, the combination of the concision of code and the richness of the standard library means that we write fewer lines of code than we would in JavaScript, which leads to more robust and maintainable applications.

## Domain Modeling with Maps and Vectors

We are now quite familiar with maps and vectors as well as some of the collection and sequence operations that can be used on them. Now we can put them in practice in a real domain: an analytics dashboard. The main concepts that we need to model are _user_, _session_, _pageview_, and _event_, and the relationships between these models are as follows:

- A user has one or more sessions
- A session has one or more pageviews and may belong to a user or be anonymous
- A pageview has zero or more events

We now know enough to create some sample data. Let's start at the "bottom" with the simplest models and work our way up to the higher-level models. Since an _event_ does not depend on any other model, it is a good place to start.

### Modeling Events

An event is some action that the user performs while interacting with a web page. It could be a _click_, _scroll_, _field entry_, etc. Different events may have different properties associated with them, but they all have at least a type and a timestamp.

```clojure
(def my-event {:type :click                                ;; <1>
               :timestamp 1464362801602
               :location [1015 433]                        ;; <2>
               :target "#some-elem"})
```

_Modeling an event_

1. Every event will have `:type` and `:timestamp` entries
2. The remaining entries will be specific to the event type

When we think of data types like _event_ in ClojureScript, we usually create at least a mental schema of the data type. There are libraries that we can use to enforce a schema on our data, most notably [clojure.spec](https://clojure.org/about/spec), but for now we will just enforce the "shape" of our data structures by convention. That is, we will ensure that whenever we create an event, we create it with a timestamp and a type. In fact, it is a common practice to define one or more functions for constructing the new data types that we create. Here is an example for how we might do this with _events_:

```clojure
cljs.user=> (defn event [type]
              {:type type
               :timestamp (.now js/Date)})
#'cljs.user/event

cljs.user=> (event :click)
{:type :click, :timestamp 1464610050488}
```

_Using a constructor function_

This function simply abstracts the process of creating a new object that follows the convention that we have established for events. We should also create a constructor function for click events specifically:

```clojure
cljs.user=> (defn click [location target]
              (merge (event :click)
                     {:location location, :target target}))
#'cljs.user/click

cljs.user=> (click [644 831] "#somewhere")
{:type :click,
 :timestamp 1464610282324,
 :location [644 831],
 :target "#somewhere"}
```

The only thing about this code that might be unfamiliar is the use of the `merge` function. It takes at least two maps and returns a new map that is the result of adding all properties from each subsequent map to the first one. You can think of it as `conj`-ing every entry from the second map onto the first.

### Quick Review: Merge

- In the REPL, define 2 maps and merge them together
- Define 3 maps and merge them together, e.g. `(merge map-1 map-2 map-3)`
- Does `merge` mutate (change) any of the maps that we pass in?
- What is the result of the following expression?

```clojure
(let [orig {:name "Cookie Monster" :food "Cookies!!"}
      overwrite {:profession "puppet" :food "Lasagne"}]
  (merge orig overwrite))
```

### You Try It

We are representing coordinates on a page with a 2-element vector containing, `[x-position, y-position]`. Define a function, `location`, that will create a location given two numbers, such that the following expressions will yield the same result:

```clojure
;; Expression 1 - Define location inline
(click [644 831] ".link")

;; Expression 2 - Construct location with a function
(click (location 644 831) ".link")
```

> *A Word on Constructors*
>
> We have been talking about the concept of _constructors_ in ClojureScript. Unlike JavaScript, constructors in ClojureScript are just plain functions that return data. There is no special treatment of constructor functions in the language - they are merely a convenience for us developers to easily create new data while consolidating the creation code in one place.

### Modeling Pageviews

With events done, we can now model pageviews. We will go ahead and define a constructor for pageviews:

```clojure
cljs.user=> (defn pageview
              ([url] (pageview url (.now js/Date) []))     ;; <1>
              ([url loaded] (pageview url loaded []))
              ([url loaded events]
                {:url url
                 :loaded loaded
                 :events events}))
3

cljs.user=> (pageview "some.example.com/url")              ;; <2>
{:url "some.example.com/url",
 :loaded 1464612010514,
 :events []}

cljs.user=> (pageview "http://www.example.com"             ;; <3>
                      1464611888074
                      [(click [100 200] ".logo")])
{:url "http://www.example.com",
 :loaded 1464611888074,
 :events [{:type :click,
           :timestamp 1464611951519,
           :location [100 200],
           :target ".logo"}]}
```

_Modeling a Pageview_

1. Define `pageview` with 3 arities
2. `pageview` can be called with just a URL
3. ...or with a URL, loaded timestamp, and vector of events

Just as we did with events, we created a constructor to manage the details of assembling a map that fits our definition of what a _Pageview_ is. One different aspect of this code is that we are using a multiple-arity function as the constructor and providing default values for the `loaded` and `events` values when they are not supplied. This is a common pattern in ClojureScript for dealing with default values for arguments.

### Modeling Sessions

Moving up the hierarchy of our data model, we now come to the _Session_. Remember that a session represents one or more consecutive pageviews from the same user. If a user leaves the site and comes back later, we would create a new session. So the session needs to have a collection of pageviews as well as identifying information about the user's browser, location, etc.

```clojure
cljs.user=> (defn session
              ([start is-active? ip user-agent] (session start is-active? ip user-agent []))
              ([start is-active? ip user-agent pageviews]
                {:start start
                 :is-active? is-active?
                 :ip ip
                 :user-agent user-agent
                 :pageviews pageviews}))
5
cljs.user=> (session 1464613203797 true "192.168.10.4" "Some UA")
{:start 1464613203797, :is-active? true, :ip "192.168.10.4", :user-agent "Some UA", :pageviews []}
```

_Modeling a Session_

There is nothing new here. We are simply enriching our domain with more types that we will be able to use in an analytics application. The only piece that remains is the _User_.

### You Try It

Now that we have walked through the definition of _events_, _pageviews_, and _sessions_, you have all of the tools that you need to define a data type for users.

- Define the "shape" of a user. It should include at least the following: `:id`, `:name`, `:sessions`.
- Create a constructor function that can create a user with or without a collection of sessions
- For extra credit, create another function called `anonymous-user` that creates a user that has no id or name

We now have a fairly complete domain defined for our analytics application. Next, we'll explore how we can interact with it using primarily functions from ClojureScript's standard library. Below is a sample of what some complete data from our domain looks like at this point. It will be helpful to reference this data as we move on.

```clojure
;; User
{:id 123
 :name "John Anon"
 :sessions [

   ;; Session
   {:start 1464379781618
    :is-active? true
    :ip 127.0.0.1
    :user-agent "some-user-agent"
    :pageviews [

      ;; Pageview
      {:url "some-url"
       :loaded 1464379918936
       :events [

         ;; Event
         {:type :scroll
          :location [403 812]
          :distance 312
          :timestamp 1464380102036}

         ;; Event
         {:type :click
          :location [644 112]
          :target "a.link.about"
          :timestamp 1464380117760}]}]}]}
```

_Sample data for an analytics domain_

## Working With Associative Data

Most of our analytics data is in the form of maps, which are simple key-value associations. As we have just seen, there is quite a lot of data that can be modeled using only maps, so it stands to reason that ClojureScript would provide good tools for operating on them. This is indeed the case. We will look at several functions that we will keep coming back to when we work with maps: `assoc`, `dissoc`, and `select-keys`. There are more function in the standard library that can be used on maps, but these are the most commonly used and deserve some explanation. [The Clojure Cheatsheet](http://clojure.org/api/cheatsheet) is an excellent reference for the functions that we will not be able to cover.

### More or Less: Adding and Removing Elements

ClojureScript has a very helpful pair of functions for adding and removing map entries: `assoc` and `dissoc`. Unlike setting and deleting JavaScript object properties, `assoc` and `dissoc` do not touch the maps that we supply. Instead, they return new maps. By now, we should be familiar with the idea of working with immutable data, but it still takes some getting used to.

#### Adding Values With `assoc`

Let's consider the _session_ model that we just created. It has identifying information about user's visit to our website. Our new requirement is to add a _duration_ to every session once the user has logged out or left the site. In this case, we just need to add a new entry to the session map - let's call it `:duration`.

![Associating Data Into a Map](/img/lesson19/assoc.png)

_Associating Data Into a Map_

This is exactly the case that the `assoc` function solves: associating some key with a value inside a map. `assoc` takes a map and a key and value to associate into the map. It can also accept any additional number of keys and values as arguments, and it will associate all of the keys and values in the map.

```clojure
cljs.user=> (def trail {:name "Bear Creek Trail"
                        :distance 7.5})
#'cljs.user/trail

cljs.user=> (assoc trail :difficulty :moderate)            ;; <1>
{:name "Bear Creek Trail",
 :distance 7.5,
 :difficulty :moderate}

cljs.user=> (assoc trail                                   ;; <2>
                   :difficulty :moderate
                   :location "Colorado"
                   :max-elevation 12800)
{:name "Bear Creek Trail",
 :distance 7.5,
 :difficulty :moderate,
 :location "Colorado",
 :max-elevation 12800}
```

_Adding Entries to a Map_

1. Adding a single entry
2. Adding multiple entries

With that, we can write a function that, given an end timestamp, will add a `:duration` entry with the number of seconds in the session:

```clojure
cljs.user=> (defn with-duration [session end-time]
              (let [duration-in-ms (- end-time (:start session))
                    duration-in-s (.floor Math (/ duration-in-ms 1000))]
                (assoc session :duration duration-in-s)))

cljs.user=> (def my-session
              (session (.now js/Date) true "127.0.0.1" "Some UA"))
#'cljs.user/my-session

;; Wait a few seconds

cljs.user=> (with-duration my-session (.now js/Date))
{:start 1464641029299,
 :is-active? true,
 :ip "127.0.0.1",
 :user-agent "Some UA",
 :pageviews [],
 :duration 14}
```

#### Quick Review: assoc

- Is there a difference between `(assoc some-map key val)` and `(conj some-map [key val])`?
- Does assoc mutate (change) the map that is passed in?

### Removing Values With dissoc

Now imagine that we have added a setting where users can request that we not track their IP or user agent, so we will need to remove this data from the map before we send it off to the server. This is exactly the functionality that `dissoc` gives us: it takes a map and any number of keys to remove from the map, and it returns a new map without the keys we specified. Let's create a function, `untracked`, that returns a session without these entries:

```clojure
cljs.user=> (defn untrack [session]
              (dissoc session :ip :user-agent))
#'cljs.user/untrack

cljs.user=> (untrack my-session)
{:start 1464641029299, :is-active? true, :pageviews []}
```

#### Quick Review: dissoc

- Use `dissoc` to remove the `:region` key from this map: `{:landmark "Uncompahgre", :region "San Juan Mountains"}`
- What happens when the map does not contain one or more of the keys that we pass to `dissoc`, e.g. `(dissoc {:temp 212} :color :material :mass)`?
- Update the `with-duration` function that we created earlier to remove the `:is-active?` key from the session.

### Refining a Selection With select-keys

Another handy function to have in our toolbox when working with maps is `select-keys`. It takes a map and a collection of keys to retain, and it returns a new map with only the keys that were passed in. If we had some portion of the application that was only interested in when a session started, whether it was active, and its pageviews, we could use `select-keys` to narrow down the data to only what we are interested in:

```clojure
cljs.user=> (select-keys my-session [:start :is-active? :pageviews])
{:start 1464641029299,
 :is-active? true,
 :pageviews []}
```

### You Try It

It is intuitive that ClojureScript considers maps to be associative. Interestingly, vectors are also associative collections that map an integer index to the element at that index:

```clojure
cljs.user=> (associative? [])
true
```

- Define a vector with several elements at the REPL
- Use `get` to retrieve the element at a specific index
- Use `assoc` to update the element at a specific index
- Try using the `merge` and `dissoc`, functions on the vector. Do the results surprise you?

## Working With Nested Data

In any but the simplest of programs, we will need to work with nested data at some point. The analytics application that we are considering in this chapter is a good example, since we have events nested inside pageviews, which are in turn nested inside sessions, which themselves are nested inside users. Using only the functions we have seen so far would be intractable at best. We will now turn our attention to several functions that allow us to work with nested data.

### Drilling Down With get-in

We have seen the `get` function a number of times for accessing a specific element in a map or a vector. It has a cousin, `get-in`, that is used for setting values that are nested deeper inside a data structure. Instead of supplying a single key for the value to get out, we supply a sequence of keys that will be looked up in turn. We can think of this sequence as a _path_ to the data that we are interested in. It is like a road map for the computer to follow to locate the data to retrieve. For instance, to get the first pageview of the first session of some user, we could use something like the following:

```clojure
(get-in user [:sessions 0 :pageviews 0])
```

_Getting Nested Data_

This will first look up the `:sessions` key on the `user` that we passed in. Next, it will get the first session (at index 0), then it will get the `:pageviews` key on this session. Finally, it will get the first of the pageviews. Notice that the get-in is really just a convenience for repeated calls to `get`:

```clojure
(get
  (get
    (get
      (get user :sessions)                                 ;; <1>
     0)                                                    ;; <2>
   :pageviews)                                             ;; <3>
  0)                                                       ;; <4>
```

1. Get the user's sessions
2. Get the first
3. Get the pageviews
4. Get the first

This concept of a _path_ is used commonly in ClojureScript to describe how to "get to" some specific piece of data. An analogy in the JavaScript world would be chained property access on some specific object:

```javascript
user.sessions[0].pageviews[0];
```

_Getting Nested Data With JavaScript_

At first glance, the JavaScript version looks at least as clear as the ClojureScript version - in fact, perhaps a bit clearer. However, one key feature of get-in is that if at any point in the path the next property does not exist, the evaluation will stop, and the whole thing will evaluate to `nil`. A more accurate JavaScript translation would be the following[^1]:

```javascript
user &&                                                    // <1>
    user.sessions &&
    user.sessions[0] &&
    user.sessions[0].pageviews &&
    user.sessions[0].pageviews[0];                         // <2>
```

1. Check every intermediate step that may be undefined
2. Only get the nested data if every step in the path to it is defined

#### Quick Review: get-in

- Fill in the blank to make this expression true

```clojure
(= "second"
   (get-in {:tag "ul"
            :children [{:tag "li"
                        :id "first"}
                       {:tag "li"
                        :id "second"}]}
           ...)
```

- What does the following expression evaluate to?

```clojure
(get-in {} [:does :not :exist])
```

### Setting With assoc-in

Just as `get-in` is a variation of `get` that allows for nested data access, `assoc-in` is a variation of `assoc` that allows for the setting of nested data. Calling `assoc-in` is very similar to calling `assoc` - the difference is that instead of supplying a simple key, we pass in a path to the data that we want to set.

```clojure
(assoc-in user
          [:sessions 0 :pageviews]                         ;; <1>
          [(pageview "www.learn-cljs.com" 123456 [])])     ;; <2>
```

1. Path to the data to update
2. Value to associate

#### Quick Review: assoc-in

- What is the result of the following:

```clojure
(assoc-in {:tag "ul"
           :children [{:tag "li"
                       :id "first"}
                      {:tag "li"
                       :id "second"}]}
          [:children 1 :class]
          "last-item")`
```

- What is the result of the following:

```clojure
(assoc-in {} [:foo :bar :baz] "quux")
```

### Updating With update-in

Now that we have seen how `get-in` and `assoc-in` work, it is time to complete our trio of functions for working with nested data with `update-in`. Like `assoc-in`, it takes a data structure and a path, but instead of taking a simple value to put into the data structure, it takes a function to apply to the existing item that it finds at the specified path. The entry at this path is then replaced with whatever the function returns. Let's consider a simple example:

```clojure
cljs.user=> (update-in {:num 1} [:num] inc)
{:num 2}
```

In this case, we specified that we wanted to operate on the element located at the path `[:num]` and increment it. This yielded a new map in which the `:num` key is the increment of the `:num` key in the original map. In this simple example, we worked with flat data, but the principle is the same for nested data. Going back to the analytics example, let's say that we wanted to add 10px to the x-coordinate of a click event. We could easily accomplish this with a single `update-in`:

```clojure
(defn add-to-click-location [click-event]
  (update-in [:location 0] #(+ 10 %)))
```

When we start building single-page apps with Reagent, we will constantly be making use of `update-in`, so it is important to make sure that we are comfortable with how to use it.

#### Quick Review: update-in

- What is the result of `(update-in {} [:foo :bar] inc)`?
- Does update-in work with both maps and vectors? Why or why not?

## Summary

We covered a lot of ground in this chapter, and we are now able to do quite a bit of data manipulation, including:

- Combining maps with `merge`
- Adding and removing single properties with `assoc` and `dissoc`
- Working with deeply nested data using `get-in`, `assoc-in`, and `update-in`

Between the sequence operations that we covered in the last lesson and the additional operations that we just learned, we can write quite intricate, data-driven programs. Next up, we'll put together all that we have learned about collections and sequences to build a contact list application that keeps its data in `localStorage`.

[^1]: JavaScript's new optional chaining feature would simplify this expression as `user?.sessions?[0]?.pageviews?[0]`
