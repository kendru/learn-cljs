---
title: "Interacting With JavaScript Data | Lesson 13"
date: 2019-10-04T23:20:24-06:00
type: "docs"
---

# Lesson 13: Interacting With JavaScript Data

One of the advantages of ClojureScript is its excellent interoperability with JavaScript. When Clojure was first introduced, one of its primary goals was providing simple integration with existing Java code. ClojureScript continues in this spirit of valuing integration with its host platform. We will deal with JavaScript interoperability to a greater extent later, but at this point, we will concern ourselves with creating and manipulating JavaScript data structures.

---

**In this lesson:**

- Convert between ClojureScript and JavaScript data types
- Integrate ClojureScript code with an existing JavaScript codebase
- Understand the implications of using mutable JavaScript objects and arrays

---

## Example: Integration With Legacy Code

Imagine that we have decided to slowly migrate a legacy JavaScript application to ClojureScript (an excellent choice). However, due to the size of the codebase, it is more practical to migrate one piece at a time. In the meantime, we need to interact with our legacy application, a classroom management application, from ClojureScript. We will need to read a list of scores from the legacy application, perform modifications in ClojureScript, and send the results back to the JavaScript application. Fortunately for us, ClojureScript has excellent interoperability with JavaScript, so let's learn how it's done!

## Using Conversion Functions

When we are working with an existing JavaScript codebase or libraries, chances are that we will be passing JavaScript data structures around, but we would like to treat them as ClojureScript data within our application. ClojureScript provides two handy functions for converting between JavaScript and ClojureScript data structures: `js->clj` for converting from JavaScript and `clj->js` for converting to JavaScript. We can easily use these functions to convert data to ClojureScript structures coming into our program and back to JavaScript on the way out.

Let's try this out by opening up the REPL and the browser tab that it is connected to. Open the dev tools and create an object called `testScores` that looks something like the following:

```javascript
var testScores = [                                         // <1>
  { id: 1, score: 86, gradeLetter: "B" },                  // <2>
  { id: 2, score: 93, gradeLetter: "A" },
  { id: 3, score: 78, gradeLetter: "C" },
];
```

_Creating a JS Object_

1. The top-level structure is an array of objects
2. The nested objects have `id`, `score`, and `gradeLetter` properties

This creates a global JavaScript variable called `testScores`, which we can access from the REPL. ClojureScript creates a namespace (think a module for collecting functions and data) called `js` that contains all of the global variables that are available within the browser. For example, we can access the `document` object with `js/document`, the `window` object with `js/window`, etc.

![Sharing Data Between Browser and REPL](/img/lesson13/sharing-data.png)

_Sharing Data Between Browser and REPL_

We can use the REPL to inspect this variable, convert it to a ClojureScript data structure, modify it and write a new version back out the `testScores` variable.

```clojure
cljs.user=> (def cljs-scores (js->clj js/testScores))      ;; <1>
#'cljs.user/cljs-scores

cljs.user=> cljs-scores
[{"id" 1, "score" 86, "gradeLetter" "B"}
{"id" 2, "score" 93, "gradeLetter" "A"}
{"id" 3, "score" 78, "gradeLetter" "C"}]

cljs.user=> (conj cljs-scores                              ;; <2>
                  {"id" 4, "score" 87, "gradeLetter" "B"})
[{"id" 1, "score" 86, "gradeLetter" "B"}
{"id" 2, "score" 93, "gradeLetter" "A"}
{"id" 3, "score" 78, "gradeLetter" "C"}
{"id" 4, "score" 87, "gradeLetter" "B"}]

cljs.user=> cljs-scores
[{"id" 1, "score" 86, "gradeLetter" "B"}
{"id" 2, "score" 93, "gradeLetter" "A"}
{"id" 3, "score" 78, "gradeLetter" "C"}]

cljs.user=> (def updated-scores                            ;; <3>
              (conj cljs-scores {"id" 4, "score" 87, "gradeLetter" "B"}))
#'cljs.user/updated-scores

cljs.user=> (set! js/testScores (clj->js updated-scores))  ;; <4>
#js [#js {:id 1, :score 86, :gradeLetter "B"}
#js {:id 2, :score 93, :gradeLetter "A"}
#js {:id 3, :score 78, :gradeLetter "C"}
#js {:id 4, :score 87, :gradeLetter "B"}]
```

_Converting between JavaScript and ClojureScript data_

1. Convert `testScores` to a ClojureScript value
2. Create a modified value by appending a new score and verify that the value in the var `cljs-scores` was not changed
3. Bind the updated scores to the `updated-scores` var
4. Convert the updated scores back to a JavaScript object and update `testScores` to the new value

We can inspect the `testScores` variable in the browser to make sure that it has been changed to include the new score.

![Checking the Updated Scores](/img/lesson13/checking-scores.png)

_Checking the Updated Scores_

### Quick Review

We still have a reference to the `js/testScores` variable.

- What will happen if we change this variable in the browser's developer tools and print it out from ClojureScript?
- Will changing this JavaScript variable affect our `cljs-scores` variable?

<blockquote>
<p><strong>Note</strong></p>
<p>Since ClojureScript has richer data types than JavaScript, <code>clj->js</code> is a <em>lossy</em> operation. For instance, sets are converted to JS arrays, and keywords and symbols are converted to strings. This means that some ClojureScript value contained in the var, <code>x</code>, is not always equal to <code>(js->clj (clj->js x))</code>. For instance, if we have a set, <code>#{"Lucy" "Ricky" "Fred" "Ethel"}</code>, and we convert this to JavaScript, we will end up with an array: <code>["Ricky", "Fred", "Lucy", "Ethel"]</code> (remember, sets are not ordered, so the order in which the elements appear when converted to an array is arbitrary). If we convert this array back to ClojureScript, we end up with the vector, <code>["Ricky" "Fred" "Lucy" "Ethel"]</code>, not the set that we started with, as we demonstrate below.</p>

<pre>
cljs.user=> (def characters #{"Lucy" "Ricky" "Fred" "Ethel"})
#'cljs.user/characters
cljs.user=> (def js-characters (clj->js characters))
#'cljs.user/js-characters
cljs.user=> js-characters
#js ["Ricky" "Fred" "Lucy" "Ethel"]
cljs.user=> (js->clj js-characters)
["Ricky" "Fred" "Lucy" "Ethel"]
cljs.user=> (= characters (js->clj js-characters))
false
</pre>
</blockquote>

### You Try It

- Create a JavaScript object from the REPL and make it available as `window.myVar`.
- Create a JavaScript object in the dev tools called `jsObj` and modify it using the `set!` function in the ClojureScript REPL

## Working with JavaScript Data Directly

Although it is very common to convert JavaScript data from the "outside world" to ClojureScript data before working with it, it is also possible to create and modify JavaScript data directly from within ClojureScript. ClojureScript numbers, strings, and booleans are the same as their JavaScript counterparts, so they can be handled natively from ClojureScript.

### Using Objects

Objects can be created either with the `js-obj` function or the literal syntax, `#js {}`.

```clojure
cljs.user=> (js-obj "isJavaScript" true, "type" "object")  ;; <1>
#js {:isJavaScript true, :type "object"}

cljs.user=> #js {"isJavaScript" true, "type" "object"}     ;; <2>
#js {:isJavaScript true, :type "object"}
```

_Constructing JavaScript Objects_

1. Creating an object with the `js-obj` function
2. Creating an object with the literal `#js {}` syntax

The `js-obj` function takes an even number of arguments, which are expected to be pairs of key, value. The literal syntax looks like a ClojureScript map proceeded by `#js`. Both of these forms produce identical JavaScript objects, but the literal syntax is by far the most common.

We can get properties on JavaScript objects with the property access syntax: `(.-property object)`, and we can use the `set!` function to update a property.

```clojure
cljs.user=> (def js-hobbit #js {"name" "Bilbo Baggins", "age" 111})
#'cljs.user/js-hobbit
cljs.user=> (.-age js-hobbit)
111
```

A variant of the property access syntax supports accessing properties inside nested objects, similar to chaining property lookups on JavaScript objects. For instance, in JavaScript, we could do the following (if we were confident that all of the intermediate properties were valid):

```javascript
// JavaScript nested lookup
var settings = {                                           // <1>
  personal: {
    address: {
      street: "123 Rolling Hills Dr",
    },
  },
};

// Prints "123 Rolling Hills Dr"
console.log(settings.personal.address.street);             // <2>
```

1. A nested JavaScript object
2. Accessing a nested property

Using property access in ClojureScript accomplishes the same task. The syntax is slightly different from a normal property access: `(.. obj -propOne -propTwo)`.

```clojure
(println
  (.. settings -personal -address -street))
; Prints "123 Rolling Hills Dr"
```

In addition to letting us read properties on a potentially nested object, ClojureScript provides the `set!` function to mutate objects. This function takes a property access along with a new value to set, and it mutates the object at the specified property, returning the value that was supplied.

```clojure
cljs.user=> (set! (.-name js-hobbit) "Frodo")              ;; <1>
"Frodo"

cljs.user=> (set! (.-age js-hobbit) 33)
33

cljs.user=> js-hobbit                                      ;; <2>
#js {:name "Frodo", :age 33}
```

1. Setting two properties on the `js-hobbit` object
2. `set!` mutates the object

#### Experiment

Since property access supports nested properties, it only makes sense that the the `set!` function would support setting nested properties. Use the REPL to try to find the correct syntax for setting the following student's grade in her Physics class:

```clojure
(def student #js {"locker" 212
                  "grades" {"Math" "A",
                            "Physics" "B",
                            "English" "A+"}})
```

Unlike the functions that we have seen that operate on ClojureScript data, `set!` actually modifies the object in-place. This is because we are working with mutable JavaScript data.

### Using Arrays

Just like there is a function and a literal syntax for creating JavaScript objects, we can use the `array` function or the `#js []` literal for creating JavaScript arrays.

```clojure
cljs.user=> (array "foo" "bar" "baz")
#js ["foo" "bar" "baz"]

cljs.user=> #js [1 3 5 7 11]
#js [1 3 5 7 11]
```

For array access, we can use the `aget` and `aset` functions. `aget` takes a JavaScript array and an index into that array and returns the element at that index. `aset` has an additional parameter, which is the value to set at the specified index. Like `set!`, `aset` mutates the array in place.

```clojure
cljs.user=> (def primes #js [1 3 5 7 11])                  ;; <1>
#'cljs.user/primes

cljs.user=> (aget primes 2)                                ;; <2>
5

cljs.user=> (aset primes 5 13)                             ;; <3>
13

cljs.user=> primes                                         ;; <4>
#js [1 3 5 7 11 13]
```

_Getting and Setting Array Elements_

1. Bind a var to a JavaScript array
2. Get the element at index `2`
3. Set the element at index `5` to `13`
4. `aset` has mutated the array

We can also access the JavaScript array methods by using, `(.functionName array args*)`. This is the standard syntax for calling a method on a JavaScript object, which we will explain in much more detail later.

```clojure
cljs.user=> (.indexOf primes 11)                           ;; <1>
4

cljs.user=> (.pop primes)                                  ;; <2>
13

cljs.user=> primes
#js [1 3 5 7 11]
```

_Using JavaScript Array Methods_

1. Call the `indexOf` method on `primes` - equivalent to `primes.indexOf(11)` in JavaScript
2. Call the `pop` method - equivalent to `primes.pop()` in JavaScript

### Quick Review

- Use the JavaScript `Array.prototype.push` function to add a value to the end of this array: `#js ["first", "second"]`
- Use the JavaScript `Array.prototype.pop` function to remove the value that you just added in the previous exercise

> Best Practice
>
> Although ClojureScript makes working with JavaScript objects and arrays simple, we should prefer to use ClojureScript data structures and only convert to and from JavaScript data at the "edges" of our program or when interacting with another library. The advantages that we get from immutable data - particularly the safeguard against all sorts of mutation-related bugs - are significant, so the more of our apps are immutable, the better.

### You Try It

Create the following variable in your browser's dev tools:

```javascript
var books = [
  {
    title: "A History of LISP",
    subjects: ["Common Lisp", "Scheme", "Clojure"],
  },
  {
    title: "All About Animals",
    subjects: ["Piranhas", "Tigers", "Butterflies"],
  },
];
```

- Write an expression that will retrieve the value, "Scheme".
- Write an expression that will have the side effect of changing the title "All About Animals" to "Dangerous Creatures".

### Challenge

Write a ClojureScript function that will take a book represented as a ClojureScript map, convert it to a JavaScript object, append it to the `books` array, and return the number of elements in `books` after adding the new book.

Possible Solution:

```clojure
(defn add-book [book]
  (let [js-book (clj->js book)]
    (.push js/books js-book)
    (.-length js/books)))
```

## Summary

ClojureScript has a symbiotic relationship with JavaScript, and to effectively use it, we must be comfortable interacting with the host language. In this lesson, we looked at how to work with JavaScript data. We used both the ClojureScript REPL and the browser's JavaScript dev tools to walk through the process of converting between ClojureScript and JavaScript data structures as well as directly modifying JavaScript objects and arrays. We are now able to:

- Create JavaScript objects from ClojureScript code
- Modify JavaScript objects and arrays
- Convert between ClojureScript's data structures and JavaScript objects and arrays
