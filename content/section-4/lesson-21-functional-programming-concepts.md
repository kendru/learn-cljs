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
that we must avoid if we want to write functional code: data mutation. When a function
mutates the data that it receives (or even worse - global data),

### Referential Transparency

A function that does not rely on any potentially mutable external state is said to be
_referentially transparent_. When a function is referentially transparent, we can be sure
that it will always produce the same output when given the same input. A common use case
is to take the current time into consideration for some computation. For instance, this
code will generate a greeting appropriate to the time of day:

```clojure
(defn get-time-of-day-greeting []
  (cond <= (.getHour (js/Date.now))
    ))
```

## Data Pipelines

## Summary

In

<!--
## Closures and Environments

### Emulating objects
We can extend our mental model to accommodate the concept of a closure by
thinking of a function as the combination of the function's body and an `environment`,
which is a table of all of the symbols that were visible when it was defined, including its
formal parameters. When evaluating the function,

// TODO: Diagram illustrating environment lookup
-->
<!-- Note: we should discuss functional purity and why effectful functions are allowed in ClojureScript -->