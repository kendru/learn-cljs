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

While we need side effects, we should strive to segregate functions that perform side effects
from those that can be pure. The pure functions can then be easily tested and reasoned about.

## Immutable Data

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