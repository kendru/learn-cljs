---
title: "Functional Programming Concepts | Lesson 21"
date: 2019-10-06T16:01:35-06:00
type: "docs"
draft: true
---

# Lesson 21: Functional Programming Concepts

ClojureScript sits at the intersection of functional programming and pragmatism. In this
lesson, we will take a deeper look at what it means to be a functional language. As we
shall see, functional programming is about much more than simply being able to use functions
as values. At its core, the more important concepts are functional purity, immutability,
and composability. The concept of functional purity means that our functions do not have
side effects like mutating global state, modifying the web page, etc. Immutability means
that instead of modifying variables in-place, we produce new, transformed values. Composability
means that we build larger modules and systems out of small, reusable pieces. These three
concepts together make for effective functional programming, and by the end of this lesson,
we will have a better understanding of what it means to write functional code in ClojureScript.

---

*In this lesson:*

- Make programs easier to reason about with functional purity
- Learn the key role that immutability plays in functional programming
- Start thinking of programs as modular data pipelines

---

## Minimizing Side Effects

Side effects are essential in every useful program. A program with no side effects could not
modify the DOM, make API requests, save data to `localStorage`, or any of the other things
that we typically want to do in a web application. Why, then, do we talk about writing code
without side effects as a good thing? The purely functional programming model does not allow
side effects, but all functional programming languages provide at least some facility for
side effects. For instance, Haskell provides the IO monad for performing impure IO operations
behind an otherwise pure functional API. ClojureScript is even more practical though, allowing
us to write effectful code as needed. If we were not careful, we could end up writing code that
has all of the same pitfalls as most JavaScript code in the wild. Since the language itself is
not going to prevent us from writing code riddled with side effects, we need to intentionally
constrain ourselves to write _mostly_ pure applications. Thankfully, ClojureScript makes that
sort of constraint easy.

<!-- TODO: Add diagram of effectful code surrounding a purely functional core -->

While we need side effects, we should strive to segregate functions that perform side effects
from those that can be pure. The pure functions can then be easily tested and reasoned about.
For instance, let's take a look at the code that we wrote for the temperature converter app
in [Lesson 15](/section-2/lesson-15-capstone-temperature-converter/):

```clojure
(defn update-output [_]
  (if (= :celsius (get-input-uom))
    (do (set-output-temp (c->f (get-input-temp)))
        (gdom/setTextContent output-unit-target "F"))
    (do (set-output-temp (f->c (get-input-temp)))
        (gdom/setTextContent output-unit-target "C"))))
```

While this code gets the job done, it is not especially clean or elegant because it both
performs a conversion and does I/O. In order to test this code, we need to run it on a
page where all of the elements exist, and in order to test it, we would have to manually
set the input fields, call the function, then assert on the content of the output element.
Instead, we could refactor this into several pure functions: a pure function to get the
label, a pure function to perform the conversion, and an impure function that reads from
and mutates the DOM.

```clojure
(defn target-label-for-input-unit [unit]                   ;; <1>
  (case unit
    :fahrenheit "F"
    :celsius "C"))

(defn convert [unit temp]                                  ;; <2>
  (if (= :celsius unit)
    (c->f temp)
    (f->c temp)))

(defn update-output [_]                                    ;; <3>
  (let [unit (get-input-unit)
        input-temp (get-input-temp)
        output-temp (convert unit input-temp)
        output-label (target-label-for-input-unit unit)]
    (set-output-temp output-temp)
    (gdom/setTextContent output-unit-target output-label)))
```

1. Extracted code for getting the converted unit label from the input unit
2. Extracted code for converting a temperature from one unit to the other
3. The impure code now only orchestrates the UI logic

Not only is the pure functional core of our code easier to test, it is also
more resilient to changes that we may want to make to the UI and user experience
application. If we wanted to change the way that the UI works, we would only need
to replace the `update-output` function, not any of the conversion logic.

## Immutable Data

We have just walked through the process of refactoring code that mutates the DOM into
an impure wrapper around a functional core. However, there is another type of side effect
that we must avoid if we want to write functional code: data mutation. In JavaScript, the two
built-in collection data structures - objects and arrays - are mutable, meaning they can
be modified in-place. All of the object-oriented features in JavaScript rely on mutability.
For example, it is very common to see code that makes directly manipulates an object, such
as the following:

```javascript
var blog = {
  title: 'Object-Oriented JavaScript',
  tags: ['JavaScript', 'OOP'],
  rating: 4
};

blog.tags.push('mutability');
blog.rating++;
blog.title += ' considered harmful';
blog.isChanged = true;
```

In ClojureScript, instead of modifying an object, we create copies of the
original object with modifications. While this sounds inefficient, ClojureScript
uses data structures that are constructed in such a way that similar objects can
often share much of their structure in the same memory locations. As we will
see, these immutable data structures actually enable highly-optimized user
interfaces and can even speed up an application. Working with immutable data
structures takes a mind shift but is every bit as easy as working with mutable
data, as we can see below.

```clojure
(def blog {:title "Functional ClojureScript"
           :tags ["ClojureScript" "FP"]
           :rating 4})

(def new-blog
  (-> blog                                                 ;; <1>
      (update-in [:tags] conj "immutability")
      (update-in [:rating] inc)
      (update-in [:title] #(str % " for fun and profit"))
      (assoc :new? true)))

new-blog                                                   ;; <2>
; {:title "Functional ClojureScript for fun and profit",
; :tags ["ClojureScript" "FP" "immutability"], :rating 5, :new? true}
blog
; {:title "Functional ClojureScript", :tags ["ClojureScript" "FP"], :rating 4}
```

1. Build up a series of transformations using the `->` macro
2. Inspect both the original `blog` and transformed `new-blog` maps

In this example, we see that the original `blog` variable is untouched. We stack
a series of modifications on top of the `blog` map and save the modified map as
`new-blog`. The key takeaway here is that none of the `update-in` or `assoc`
functions touched `blog` - they returned a new object that was similar to the
one passed in but with some modification. Immutable data is key to functional
programming because it gives us the assurance that our programs are repeatable
and deterministic (at least the parts that need to be). When we allow data to
be mutated as it is passed around, complexity skyrockets. When we allow mutable
data, we need to keep track of a potentially enormous number of variables in
our heads to debug a single computation.

> *Note:*
>
> The author once worked on a team responsible for a very large JavaScript
> application. That team discovered that unexpected mutation was the cause of so
> many defects that they started keeping a tally of every hour spent finding and
> fixing bugs that were due to mutable data. The team switched to the Immutable.js
> library when their tallies no longer fit on the whiteboard.

### Referential Transparency

A function is said to be _referentially transparent_ if it fits in the pure substitution model
of evaluation that we have discussed. That is, a call to a function can always be replaced with
the value to which it evaluates without having any other effect. This implies that the function
does not rely on any state other than its parameters, and it does not mutate any external state.
It also implies that whenever a function is called with the same input values, it always produces
the same return value. However, since not every function can be referentially transparent in a real
application, we apply the same strategy of keeping our business logic pure and referentially
transparent while moving referentially opaque code to the "edges" of the application. A common need
in many web apps is to take the current time into consideration for some computation. For instance,
this code will generate a greeting appropriate to the time of day:

```clojure
(defn get-time-of-day-greeting []
  (condp >= (.getHours (Date.))
    11 "Good morning"
    15 "Good day"
    "Good evening"))
```

The problem with this code is that its output will change when given the same input (in
this case, no input) depending on what time of day it is called. Getting the current time
of day is intrinsically not referentially transparent, but we can use the technique that we
applied earlier to separate effectful functions from a functionally pure core of business
logic:

```clojure
(defn get-current-hour []                                  ;; <1>
  (.getHours (Date.)))

(defn get-time-of-day-greeting [hour]                      ;; <2>
  (condp >= hour
    11 "Good morning"
    15 "Good day"
    "Good evening"))

(get-time-of-day-greeting (get-current-hour))
;; "Good day"
```

1. Factor out the function that is not referentially transparent
2. Ensure that the output of our business logic is solely dependent on its formal parameters

This is a fairly trivial example, but any code that relies on external state - whether it is
the time of day, the result of an API call, the `(Math.random)` random number generator,
or anything other than its explicit parameters - breaks the functional paradigm and is more
difficult to test and evolve as requirements change.

## Summary

In this lesson, we looked briefly at three cornerstones of functional programming: minimizing
and segregating the side effects, using immutable data, and keeping our business logic
referentially transparent. When we these ideas, we naturally write programs that take a piece
of data, pass it through a number of transformations, abd the result is the output of the program.
In reality, most programs are made up of many pure data pipelines that are glued together with impure
code that interacts with the DOM, but if we follow the functional programming concepts that we have
learned in this lesson, we will end up with a clean functional core of business logic that is primarily
concerned with data transformation. Over the next few lessons, we will learn more language features
and techniques that will enable us to be effective practitioners of functional programming.
