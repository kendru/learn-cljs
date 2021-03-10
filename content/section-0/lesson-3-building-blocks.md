---
title: "Building Blocks | Lesson 3"
type: "docs"
date: 2019-09-19T12:49:41-06:00
---

# Lesson 3: Building Blocks

With an understanding of what ClojureScript is and why it matters, we will begin our journey with an overview of the basics of the language. One of the biggest hurdles in learning an unfamiliar language is understanding the syntax. While there is a great deal of crossover between languages at the conceptual level, the way that those concepts are expressed can be quite different. Over the next two lessons, we will hone in on the foundational skill of reading ClojureScript code. Even though the syntax is so simple compared to JavaScript, it looks very unusual to most programmers who have cut their teeth on C-like languages (including JavaScript). The skill of reading ClojureScript will go a long way towards being able to read the longer code samples in the coming lessons with ease.

---

**In this lesson:**

- Learn the basics of ClojureScript's syntax
- Understand the purpose of all the parentheses
- Survey the core data types that are built in to the language

---

First, in this lesson, we will survey the most common syntactic elements of the language, then in the next lesson we will take a look at how ClojureScript code is actually evaluated. Understanding the evaluation model will help us both understand the code we read and write code that does exactly what we expect it to. ClojureScript is a very small language - much smaller than JavaScript - so it is very simple to understand. Despite the foreign syntax, we can reach a point of familiarity surprisingly quickly due to the relatively few syntactic elements of the language.

## Parens, Parens Everywhere!

As we have seen in the examples of the previous lessons, ClojureScript code is replete with parentheses. For many, this single aspect of the language is what makes it seem intimidating. Parentheses are the primary symbols used for delineating one piece of code from another. Consider that JavaScript and other languages in the C family use both parentheses and curly brackets - parentheses to indicate parameters to a function and to specify the order of operations and curly brackets to set apart blocks of related statements. Once we get over the initial "paren shock", ClojureScript begins to look simple, even elegant.

### Expressions and Function Evaluation

Parentheses are used in ClojureScript to indicate expressions to be evaluated. We will look much deeper into these so called `s-expressions` in the next lesson, but they are so critical that we must at least mention them here. At a high level, every ClojureScript program has basically the following form:

#### ClojureScript Program Distilled

```clojure
(some-function arg1 arg2 ...)
```

Whenever there is an open parenthesis, the next thing that the compiler expects is something that can be called - usually a function. Everything else until the next closing parenthesis is expected to be an argument.

![Simple Expression](/img/lesson3/simple-expression.png)

_Simple Expression_

If we were to write the same general structure in JavaScript, it would look something like the following:

```javascript
someFunction(arg1, arg2, ...);
```

Both the ClojureScript and JavaScript code indicate that there is some function that should be called with some number or arguments. While the JavaScript code indicates a function call by putting the name of the function first, followed by some arguments enclosed in parentheses, ClojureScript indicates the same thing by enclosing both the function name and its arguments within a set of parentheses.

## Core Data Types

ClojureScript has all of the primitive data types that we would expect from any programming language: numbers, strings, booleans, and the like. We refer to these simple values as scalars. Additionally, the language has a variety of useful _collection_ types as well - think arrays and objects from JavaScript. These collections are so frequently used that there is special syntax for representing them. Before diving into each of the data types, it bears enumerating a complete list of data types for reference. The following table lists the types that have a literal syntactic representation, along with a brief description and an example of how it is expressed in code.

#### ClojureScript Data Literals

| Data Type | Description                                                     | Example                                  |
| --------- | --------------------------------------------------------------- | ---------------------------------------- |
| Number    | Integer or floating point numbers                               | `17.4`                                   |
| String    | Textual data                                                    | `"Today is the first day..."`            |
| Character | Textual data                                                    | `\a`                                     |
| Boolean   | Logical true/false                                              | `true`                                   |
| Keyword   | Lightweight identifiers                                         | `:role`                                  |
| Symbol    | Identifiers that are extensively used internal to ClojureScript | `'cljs-is-awesome`                       |
| List      | Ordered collection supporting efficient traversal               | `'(1 3 5 7 9)`                           |
| Vector    | Ordered collection supporting efficient access by index         | `[0 2 4 6 8]`                            |
| Map       | Unordered collection associating unique keys to values          | `{:name "Kayleigh", :age 29}`            |
| Set       | Unordered collection of unique values                           | `#{true "ubiquity" 9.2}`                 |
| nil       | The empty value                                                 | `nil`                                    |
| Object    | JavaScript object - used for interop                            | `#js {"isJs" true, "isImmutable" false}` |
| Array     | JavaScript array - user for interop                             | `#js ["Lions" "Tigers" "Bears"]`         |

We will now look at each data type in turn and see a few examples of its usage so that we can identify the various elements in any given piece of ClojureScript code.

### Numbers

ClojureScript uses JavaScript's Number primitive, so it can support exactly the same integer and floating point numbers that JavaScript does. Below are examples of the different formats that ClojureScript recognizes as valid numbers.

#### Numbers

```clojure
32                                                         ;; <1>

012                                                        ;; <2>

0xbeef                                                     ;; <3>

0.6                                                        ;; <4>

1.719493e3                                                 ;; <5>

-0.12e-4                                                   ;; <6>
```

1. Decimal integer
2. Octal integer starts with a leading zero
3. Hexadecimal integer starts with leading `0x`
4. Float
5. Float with an exponent
6. Float with a sign and exponent with a sign

### Strings

Strings, like numbers, use JavaScript primitives. However, ClojureScript's string syntax is more restricted than JavaScript's. Notably, strings _must_ be contained in double quotes, since ClojureScript uses single quotes for other purposes. Double quotes and other special characters are escaped with a backslash.

#### Strings

```clojure
"Quick! Brown foxes!"                                        ;; <1>

\a                                                           ;; <2>

"Column 1\tColumn 2"                                         ;; <3>

"foo
bar"                                                         ;; <4>
```

1. Simple string
2. Single character strings can be represented by the character proceeded by a backslash
3. String with special character
4. Strings can span multiple lines

### Booleans

ClojureScript also uses JavaScript booleans. Since the only possible options for a boolean are `true` or `false`, we will forego an extended example.

### Keywords

We now encounter a data type that does not have a JavaScript equivalent. A keyword is represented by a name preceded by a colon. Keywords evaluate to themselves, and two keywords with the same name are considered equal. One interesting property of keywords is that they can be used as a function. When used as a function, the keyword expects a map as an argument and it will return the value in the map for which it is the key. When a keyword begins with two colons, the current namespace will be prepended to the keyword.

#### Keywords

```clojure
:a-keyword                                                  ;; <1>

::namespaced-keyword                                        ;; <2>

:explicit-ns/keyword                                        ;; <3>

{:name "Bill", :type "admin"}                               ;; <4>

(:type user)                                                ;; <5>
```

1. Simple keyword
2. With implicit namespace - shorthand for `:cljs.user/namespaced-keyword`
3. With explicit namespace
4. Used as keys in a map
5. Used as a function to perform a map lookup

### Symbols

Symbols are an interesting data type because they are closely linked to the Lisp family of programming languages from which ClojureScript is derived. Symbols are names that usually evaluate to some other object. We have seen symbols in almost every example without even thinking about it.

```clojure
my-function                                                 ;; <1>

first                                                       ;; <2>
```

1. Symbol referring to a user-defined variable
2. Symbol referring to a built-in function

Of ClojureScript's data types, symbols are probably the most difficult to comprehend. They have a very meta quality about them, and they do not directly correspond to another familiar concept. Since they are not used very commonly in application code, we will not revisit symbols to the depth that we will with the other data types.

### Lists

Lists are comprised of a number of expressions inside parentheses. However, remember that s-expressions are also written the same way. For this reason, we designate a list that should not be evaluated as an s-expression by placing a quote before it. It is interesting to note that ClojureScript code is actually made up of lists.

#### Lists

```clojure
(+ 1 2 3 4)                                                 ;; <1>

'(+ 1 2 3 4)                                                ;; <2>

'(some data)                                                ;; <3>

'()                                                         ;; <4>
```

1. A list that is interpreted as an expression and evaluated
2. Prevent evaluation of a list by starting it with a single quote
3. Lists can contain any ClojureScript data type
4. An empty list

### Vectors

Vectors are comprised of a number of expressions contained inside square brackets. When ClojureScript encounters a vector, it will interpret it as a data structure and will not try to evaluate it as a function call. They are used in a similar manner to JavaScript arrays and are the most common data structure in ClojureScript. Vectors are also used to list the arguments that a function takes.

#### Vectors

```clojure
[]                                                          ;; <1>

["Alice" "Bob" "Carol"]                                     ;; <2>

(defn say-hello [name]                                      ;; <3>
  (println "Hello," name))
```

1. An empty vector
2. A vector used to define a collection of strings
3. A vector used to declare a function's argument list

### Maps

Maps are collections similar to a JavaScript object. They associate unique keys with values and can subsequently be used to lookup values by key. The syntax for a map is even similar to that of a JavaScript object, since it consists of a number of key-value pairs inside curly brackets. Either commas or newlines are often used to separate pairs. Commas are whitespace in ClojureScript, and we will frequently find them omitted.

#### Maps

```clojure
{}                                                          ;; <1>

{"product" "Self-Sealing Stem Bolt"                         ;; <2>
 "sku" "DS9-SB09"
 "stock" 212}

{:name "Jorge", :age 29}                                    ;; <3>
```

1. An empty map
2. A map using strings as keys
3. A map using keywords as keys

### Sets

Sets are an unordered collection of unique elements. They are often used when we want to avoid duplicates or need to quickly determine whether an element is in a collection. Sets are declared with any number of elements contained inside curly brackets that are prefixed with a pound sign.

#### Sets

```clojure
#{}                                                         ;; <1>

#{"admin" "editor" "author" "subscriber"}                   ;; <2>
```

1. An empty set
2. A set with several unique strings

Of the data structures that have their own syntax, sets are probably the least often used. It is still important to be able to recognize them, since at first glance they look quite similar to a map.

### Nil

Nil is the empty value and is always written as `nil`. It is the equivalent of `null` in JavaScript and acts the same as `false` when used as a boolean.

The JavaScript interop forms will be covered in a later lesson, so we will defer discussion until that point.

### Quick Review

- Which collection type is most similar to a JavaScript object?
- Which collection type is most similar to a JavaScript array?
- Google a ClojureScript library in a domain that is interesting to you, and look over the source code. Can you identify most of the syntactic elements?

## Summary

In this lesson, we got our first real taste of ClojureScript code, surveying the basic structure and core data types of the language. We also took a first look into expressions, the core building block of ClojureScript. In fact, expressions are so critical that the entire next lesson will be devoted to them. We now know about:

- How parentheses are used to evaluate functions
- The scalar data types: number, string, boolean, keyword, and symbol
- The collection data types: list, vector, map, and set
- The empty value: `nil`
