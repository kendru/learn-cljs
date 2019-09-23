---
title: "Reusing Code With Functions | Lesson 12"
type: "docs"
date: 2019-09-22T07:00:50-06:00
---

# Lesson 12: Reusing Code With Functions

ClojureScript is a functional programming lnguage. Love it or hate it,
the functional programming paradigm gives us superpowers, but it also
makes certain demands on the way that we write code. We have already
discussed some of the implications of functional code - immutable
data, minimizing side effects, etc. - but up to this point, we have
not studied what a function _is_, much less how to use them
idiomatically. In this lesson, we will review what a function is, and
we will study how to define and use functions. Finally, we'll look at
some best practices for when to break code into separate functions and
how to use a special class of function that is encountered often in
ClojureScript - the recursive function.

---

*In this lesson:*

- Learn ClojureScript's most fundamental programming construct
- Write beautiful code by extracting common code into functions
- Solve common problems using recursion

---

## Understanding Functions

// Explain functions as building blocks that describe transformation
// of one piece of data to another

// Diagram illustrating data transformation

// Explain functional programming as about data in motion - contrast
// with statements and assignment

## Defining and Calling Functions

// Lexical scoping

// Namespace-global w/ implicit export (because Clojure is about
// visibility and composing small pieces - expose the pieces, not
// just the highest-level abstractions)

// Importing

### fn and defn

### Functions as Expressions

// Explain that a function definition is itself an expression, and it
// can be used directly at a call site. Example

// Contrast with a method, which has an implicit mutable context (this)


###= Anonymous Functions

### Closures

// Exercise: emulating objects

### Quick Review

- Define a function using `my-inc` that returns the increment of a
number. How would you define a function with the same name without
using `defn`?

## Extracting Expressions

// Primary means of refactoring is identifying common patterns and
// extracting them into a function

### Rules of Thumb

// When you need a name

// Multiple uses

// Avoid when there is a single library function or simple composition
// of functions that accomplishes the same thing

## Recursion 101

// Example: factorial (of course)

## Summary

