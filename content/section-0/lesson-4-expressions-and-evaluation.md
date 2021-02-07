---
title: "Expressions and Evaluation | Lesson 4"
type: "docs"
date: 2019-09-19T12:57:04-06:00
---

# Lesson 4: Expressions and Evaluation

As we briefly touched on in the previous lesson, the concept of an _expression_ is at the core of ClojureScript code. For programmers coming from a language like JavaScript, thinking in terms of expressions requires a shift in perspective, but like most aspects of ClojureScript, we will find that programming with expressions is quite simple once we get used to it.

---

_In this lesson:_

- Learn about s-expressions and identify them
- Define the difference between a statement and an expression
- Understand ClojureScript's model of evaluation

---

## Laying the Foundation with S-Expressions

Most ClojureScript code is represented with a construct called an s-expression. S-expression is short for "symbolic expression", and the term comes from the old Lisp family of languages that inspired Clojure.

### Structuring an S-Expression

An s-expression can take two forms:

1. A primitive value, such as `12`, `true`, or `"tacos"`
2. A parenthesized list containing zero or more expressions separated by whitespace: `( expression* )`

With just these two forms, we have defined most of the ClojureScript's syntax. There is no special syntax for blocks, loops, function calls, conditionals, or almost any other part of the language. As we will discuss shortly, ClojureScript departs from most Lisps in that it adds a few additional syntactic elements to make the code more readable, but the simple s-expression is by far the most basic and prevalent syntactic construct. We will now turn to several examples of s-expressions.

```clojure
5                                                          ;; <1>
;; 5

+                                                          ;; <2>
;; #object[cljs$core$_PLUS_ ...]

()                                                         ;; <3>
;; ()

(+ 5 5)                                                    ;; <4>
;; 10

(take 5 (range))                                           ;; <5>
;; (0 1 2 3 4)

(map inc (take 5 (range)))
;; (1 2 3 4 5)

(mk-sandwich "Bacon" "Lettuce" "Tomato")                   ;; <6>
;; WARNING: Use of undeclared Var cljs.user/mk-sandwich at line 1 <cljs repl>
;; #object[TypeError TypeError: Cannot read property 'call' of undefined]
```

_S-Expressions_

1. A primitive
2. A function name
3. An empty s-expression
4. An s-expression consisting of other simple s-expressions
5. S-expressions can be nested
6. Just because an s-expression is syntactically valid does not guarantee that it will run.

At this point, we can begin to see that all of the parentheses serve a purpose after all (even this author had his doubts at first). They provide a consistent and explicit structure for evaluating any code. While other programming languages generally have separate syntax for function calls, math and logic operations, conditionals, method calls, etc., there is only one syntactic construct in ClojureScript, with clearly defined rules for evaluation. We will walk through the rules for how an s-expression is evaluated, but first, we will take a brief detour to discuss the emphasis on _evaluation of expressions_ rather than _execution of statements_.

> **NOTE**
>
> Using a language based on s-expressions means that there is no such thing as operator precedence. In JavaScript, we must recall that `*` has higher precedence than `+`, that `&&` has higher precedence than `OR`, and that `!` has a higher precedence than any of the other operators listed here. In ClojureScript, we do not need a chart because precedence is explicit in the syntax of the language itself. For instance, there is no question about the meaning of the expression `(and x (or y z))`, and it is also clear that `(or (and x y) z)` means something else entirely. What at first looked like weird syntax is proving to be quite useful!

### Understanding Expressions

Being a functional programming language, ClojureScript emphasizes _expressions_ rather than statements. That is, everything in ClojureScript _evaluates_ to some concrete value. Whereas JavaScript allows statements that do not yield a value and functions that do not return anything (rather, that return `undefined`, every piece of ClojureScript code - from a simple number to an entire program - is evaluated to produce some value.

Let us consider some JavaScript statements that are not expressions and do not return anything:

```javascript
const x = 5;

if (10 % 2 === 0) {
  evenOrOdd = "Even";
}

for (let i = 0; i < 10; i++) {
  console.log("Looping!");
}
```

In each of these examples, we see a piece of code that performs some computation, but it does not give a specific result. One way to think about it is that if we put a `const foo =` before any of these statements, it would lead to a syntax error, as in the following example:

```javascript
const foo = if (10 % 2 === 0) {
    evenOrOdd = "Even";
}

// Uncaught SyntaxError: Unexpected token if
```

In ClojureScript, on the other hand, there are no statements, only expressions, so absolutely everything has a value (even if that value is `nil`). This simplifies things quite a bit, as we do not have to divide the language into expressions, which evaluate to some value, and statements, which are executed, not evaluated. _Everything_ has a value. Below are the ClojureScript equivalents of the JavaScript statements that we just considered.

```clojure
(def x 5)
;; #'user/x                                                   <1>

(if (even? 10) "Even" "Odd")
;; "Even"                                                     <2>

(doseq [i (range 5)]
  (println "Looping!"))
;; Looping!
;; Looping!
;; Looping!
;; Looping!
;; Looping!
;; nil                                                        <3>
```

_ClojureScript Expressions_

1. Defining a var evaluates to the var itself
2. An if expression evaluates to the appropriate branch
3. `doseq` evaluates to `nil`

Imagine building a contact list app, and you would like to display the number of users missing phone numbers somewhere in the UI. A typical way to do this in JavaScript would be to create a counter variable then loop through the list of users, incrementing the counter if the phone number was missing. Finally, the contents of some element would be updated with the value of the counter.

```javascript
let counter = 0;
const users = [
  /* ... */
];

for (let user of users) {
  if (isMissingPhone(user)) {
    counter++;
  }
}

someElem.innerHTML = counter;
```

_Statement-Oriented JavaScript code_

This code reads like an instruction manual for the computer to follow - a list of things to execute in order to accomplish some task. When programming with expressions, on the other hand, we think about the data that we have and how we can derive from it the value that we are interested in. In this case, we have a list of users, and we are interested in the number of users missing phone numbers. With the expression-oriented approach, we would probably do something like the following, which creates a filtered list of users - containing only those missing a phone number - and then gets the number of items in that filtered list.

```clojure
(set! (.-innerHTML someElem)
      (count
        (filter missing-phone? users)))
```

_Expression-Oriented ClojureScript code_

In addition to being shorter, this code draws a clearer connection between the data that we start with (a collection of `users`) and the data that we want (the count). Interestingly, the _entire_ expression above evaluates to the number of users missing phone numbers.

![Comparing Expressions and Statements](/img/lesson4/comparing-expressions-and-statements.png)

_Comparing Expressions and Statements_

We see that the expression-oriented code returns a value at the end. However, since each expression is composed of other expressions, each step in the process has some value. Now that we have a good understanding of what expressions are and how they differ from statements, we can dig into how ClojureScript _evaluates_ (derives values) from expressions.

### Quick Review

For each of the following JavaScript code snippets, identify if it is an expression or a statement:

```javascript
// <1>
if (age === 16) {
  sweetSixteen = true;
}

// <2>
console.log('Regardless');

// <3>
'happy birthday to you'
  .split(" ")
  .map(_.capitalize)
  .join(" ");

// 4
const x = 12;

// 5
count >= threshold ? 'Too High' : 'Ok';

// 6
function foo() {}

// 7
(function () {});
```

_Answers:_ _1_ - Statement; _2_ - Statement; _3_ - Expression; _4_ - Statement; _5_ - Expression; _6_ - Statement; _7_ - Expression

## Evaluating ClojureScript Code

ClojureScript's rules for evaluating expressions are simple:

1. If the expression is a primitive element or data structure, its value _is_ that element.
2. If the expression is a parenthesized list of expressions, the first expression is interpreted as a function and the rest of the expressions are interpreted as arguments.
3. Evaluate the inner expressions first and work outwards

As an example, we will look at the expression, `(map inc (take 5 (range)))`. This s-expression is a parenthesized list of expressions, so the first element, the symbol `map`, is interpreted as a function with 2 arguments: `inc` and `(take 5 (range))`.

![Evaluating an Expression, Step 1](/img/lesson4/eval-step-1.png)

_Evaluating an Expression, Step 1_

`inc` is a function that takes an integer and returns the next-higher number. ClojureScript can call this function directly so this argument does not need to be evaluated. However, the argument, `(take 5 (range))` must be evaluated so that its value can be passed back into the `map` expression. Remembering the rules of s-expressions, we can see the ClojureScript will interpret `take` as a function with `5` and `(range)` as its arguments.

![Evaluating an Expression, Step 2](/img/lesson4/eval-step-2.png)

_Evaluating an Expression, Step 2_

The original s-expression is almost ready to be evaluated, but first, we must evaluate the final inner s-expression, `(range)`. This s-expression has only a single expression inside it, `range`, so it will be interpreted as a function with no arguments.

![Evaluating an Expression, Step 3](/img/lesson4/eval-step-3.png)

_Evaluating an Expression, Step 3_

Finally, the expression will be evaluated "inside out", starting with the call to the `range` function and working outwards to the outermost s-expression.

![Evaluating an Expression, Step 4](/img/lesson4/eval-step-4.png)

_Evaluating an Expression, Step 4_

The call to `range` returned an _infinite_ sequence of integers, starting with `0`. We will get into how to work with infinite sequences later, but for now, we just need to understand that an infinite sequence is an object that can continue to produce as many values as we need. As this expression is evaluated, the infinite sequence is substituted in place of the original expression, `(range)`, and the evaluation continues outwards.

![Evaluating an Expression, Step 5](/img/lesson4/eval-step-5.png)

_Evaluating an Expression, Step 5_

The next expression is interpreted as a call to the `take` function, with the arguments, `5` and an infinite sequence of numbers. The value of this expression is the sequence, `(0 1 2 3 4)`, the first 5 element of the infinite sequence generated by `(range)`. The call to `take` is replaced with this return value, and evaluation continues outwards again.

![Evaluating an Expression, Step 6](/img/lesson4/eval-step-6.png)

_Evaluating an Expression, Step 6_

Finally, the last step of the evaluation is performed, calling `map` with the arguments, `inc` and `(0 1 2 3 4)`. This increments every element in the sequence, and returning the final value of the s-expression.

The rules of evaluating ClojureScript are simple enough that we could work out in a few steps how an expression is evaluated. The amazing thing about this is that no matter how large and involved the code that we are looking at, we can use the same process to read almost any piece of ClojureScript. Once the basic syntax is understood, most of what remains to learn is the vocabulary and common idioms.

## Order of Operations

It may come as a surprise, but ClojureScript has no concept of operator precedence. That is, there are no rules indicating that multiplication should be performed before addition or any such thing. Instead of having a set of rules that implicitly determine the order in which to evaluate an expression, we specify the order by how we nest s-expressions. For example, the following code multiplies 5 and 2 before adding the result to 10:

```clojure
(+ 10 (* 5 2)) ; 20
```

On the other hand, the next bit of code adds 10 and 5 before multiplying the result by 2:

```clojure
(* (+ 10 5) 2) ; 30
```

The use of parentheses to determine the order of operations forces us to be more explicit and virtually eliminates the entire class of bugs related to operator precedence.

### You Try it

- Write the expression that would call a function named `make-dessert` with the arguments `"ice cream"` and `"brownies"`.
- Write the following mathematical expression in ClojureScript such that the multiplication is performed before addition and subtraction: `8 + 3 * 4 - 10`.
- Write same expression as the last exercise but such that the multiplication is performed last.

## Summary

In this lesson, we learned what expressions are and how expression-oriented programming differs from statement-oriented programming. This led us to an examination of ClojureScript's evaluation strategy, which simplifies expressions from the inside out. Finally, we learned how the s-expression syntax eliminates the need for operator precedence by making the order of every operation explicit. We can now:

- Understand how ClojureScript will evaluate our code
- Define the difference between _execution_ of a set of statements and _evaluation_ of an expression
- Read ClojureScript's s-expression syntax
