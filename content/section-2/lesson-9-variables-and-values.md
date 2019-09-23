---
title: "Using Variables and Values | Lesson 9"
type: "docs"
date: 2019-09-19T20:50:03-06:00
---

# Lesson 9: Using Variables and Values

ClojureScript takes what we think we know about variables and turns it on its
head. Instead of thinking about variables that may be modified, we should start
thinking about values that cannot be changed. While ClojureScript has the
concept of a variable (called a `var`), we cannot usually change that value that
a variable refers to. ClojureScript is careful to draw a distinction between a
var and its _value_. Just like in JavaScript, variables may be redefined to
refer to a different object; but unlike JavaScript, the object that the variable
refers to cannot be changed. It is this core idea of programming with _values_
that makes ClojureScript so interesting. In fact, values are at the core of
every functional programming language, and we will find the the combination of
immutable values and pure functions (which we will discuss in lesson 12 then
again in Unit 4) enable a style of programming that is very easy to reason
about.

---

*In this lesson:*

- Understand the difference between an immutable value and a mutable variable
- Learn the two primary ways of naming values - `def` and `let`
- Explain the value or programming with values

---

## Understanding Vars

A var is very similar to a JavaScript variable. It is a mutable reference to
some value. The fact that it is mutable means that we can have it refer to one
value initially and later refer to someone else.

Imagine going to a party where every person is a stranger to everyone else. When
you walk in the door, you are given a name tag on which to write your name.
Chances are, the name that you write on your name tag will be the name that the
other partygoers will use to address you. Now imagine that you swap name tags
with another attendee who had a different name. You as a person will remain
unchanged. Receiving a new name tag does not change your identity, only the name
that others will use to refer to you. Additionally, people are now using the
name from your original name tag to refer to someone else. Just because the
name tag does not belong to you anymore does not mean that it is invalid.

![Binding a Var to a Value](/img/lesson9/binding-var.png)

_Binding a Var to a Value_

This fictional situation is an analogy to how vars and values work - the values
are the people at the party, and the vars are the name tags. Just as names may
be changed without affecting the people who bear them, vars may be changed
without affecting the values that they name. The process of associating a var
and a value is called _binding_ the var to a value. Please feel free to follow
along in the REPL.

```clojure
(def my-name "Fred")                              ;; <1>
;; #'cljs.user/my-name

my-name
;; "Fred"

(defn mk-global [value]
  (def i-am-global value))
;; #'cljs.user/mk-global

mk-global                                         ;; <2>
;; #object[ ... ]

(mk-global [4 8 15 16 23 42])
;; #'cljs.user/i-am-global

i-am-global                                       ;; <3>
;; [4 8 15 16 23 42]

(def ten 10)
;; #'cljs.user/ten

(def twenty (* ten 2))                            ;; <4>
;; #'cljs.user/twenty

twenty
;; 20

ten                                               ;; <5>
;; 10
```

_Defining vars_

1. Binding the var, `my-name` to the value `"Fred"`
2. `defn` created a function and bound it to the var, `mk-global`
3. Even though the `i-am-global` var was defined _inside_ the `mk-global` function, it is global to the `cljs.user` namespace
4. Since expressions evaluate to values, `twenty` gets bound to the result of `(* ten 2)`, or `20`
5. We verify that the value of ten was not changed when we multiplied it by 2

### Symbols

In lesson 3, we looked very briefly at symbols, which are essentially names that
refer to something else, usually a var. In the REPL session above, `my-name`,
`mk-global`, `i-am-global`, `ten`, and `twenty` are all symbols. That is, they
are names that refer to the var that we have bound. When ClojureScript is
evaluating our program code and comes across a symbol, it will try to evaluate
the symbol to whatever it refers to, and if it cannot resolve the symbol to any
know value, it will display a warning.

```clojure
(def x 7)                                         ;; <1>
;; #'cljs.user/x

x                                                 ;; <2>
;; 7

'x                                                ;; <3>
;; x

(defn doubler [x] (* 2 x))                        ;; <4>
;; #'cljs.user/doubler

(doubler 3)
;; 6

y                                                 ;; <5>
;; WARNING: Use of undeclared Var cljs.user/y at line 1 <cljs repl>
;; nil
```

_Symbols_

1. Use the symbol `x` to refer to a var
2. The symbol evaluates to the thing it refers to
3. A quote before the symbol causes ClojureScript to evaluate the symbol _itself_, not the thing it refers to
4. Within the function, the symbol `x` refers to the function parameter, not the global var
5. Warning when trying to evaluate a symbol that does not refer to anything

#### You Try It

Almost everything in ClojureScript is a value, and a var can be bound to any
value. With this knowledge, use `def` to create a var that refers to this
function:

```clojure
(fn [message]
  (js/alert (.toUpperCase (str message "!!!!!!!!!!!!!!!!"))))
```

Can you use the var that you created to call this function? E.g. `(my-var "inconceivable")`

> *You Already Know How to Use It*
>
> In JavaScript, we already work with immutable data on a daily basis. Strings and
> numbers in JavaScript are immutable - they are values that can not be changed.
> We can _derive_ new values from them, but we (thankfully) can't say that `1 = 2`
> or `"Unchangeable" += "... or not"`. It is perfectly natural for us to think
> about these sorts of values as immutable, but we have a more difficult time
> thinking about collections as immutable. More seasoned programmers who have
> encountered immutable data structures may tend to think of them as "bulky" or
> resource-intensive (and many implementations of them are indeed inefficient).
> Whether we are simply used to mutable collections from other languages or have
> a notion of immutable collections as being impractical, it takes a while to get
> into the habit of working with immutable collections However, once we get used
> to it, thinking of maps, vectors, et al. as values becomes as natural as
> thinking about strings and numbers in the same way.

## Creating Local Bindings With let

While `def` creates a var that is visible to an entire namespace, we sometimes
want to name and use values that are more temporary or focused in
scope. ClojureScript uses `let` to create these local bindings. Like vars, let
maps a name to some value, but they do not stick around after the contents of
the `let` are evaluated. This can be useful for when we want to name things for
convenience in the middle of a function without polluting the namespace with a
bunch of unnecessary vars. The form of a `let` expression is as follows:

```clojure
(let [bindings]
  expr1
  expr2
  ...
  expr-n)
```

`bindings` are pairs of names and values, such as `[a 20, b 10, c (+ a b)]`, and
the entire `let` expression evaluates to the value of the last expression inside
the body of the `let`. Since only the value of the last expression is
considered, the other expressions are only used for side effects, such as
printing to the console or doing DOM manipulation. Here is an example of how we
might use let in a real application:

```clojure
(defn parse-msg [msg-raw]
  (let [msg-types {:c ::control
                   :e ::event
                   :x ::error}
        msg (reader/read-string msg-raw)
        type (:t msg)
        data (:d msg)]
    (println "Got data:" data)
    [(get msg-types type) data]))
```

There are a couple of important things to notice here. First, the names that we
created with the `let`, `msg-types`, `msg`, `type`, and `data`, are only defined
for code inside the `let` and will be garbage collected when the `let` completes
evaluation. Second, the names that we declare first are available in later
bindings. For example, we defined `msg` as the result of evaluating the
expression, `(reader/read-string msg-raw)`, and then we defined `type` and
`data` in terms of `msg`. This is perfectly normal and allows us to write much
clearer and concise code.

### Quick Review

- What happens when let creates a binding with the same name as a var that is already defined? What will be the output of the following code?

```clojure
(def name "Napoleon")

(let [name "Pedro"]
  (println "Vote 4" name))
```

- Fill in the following function so that it tells you the name of your favorite dessert:

```clojure
(let [desserts ["Apple Pie" "Ice Cream Sandwiches" "Chocolates" "Berry Buckle"]
      favorite-index 1
      favorite-dessert (get desserts favorite-index)]
  (println "All desserts are great, but I like" favorite-dessert "the best"))
```

## Summary

We have now gone over the two primary means of naming things in ClojureScript -
`def` for namespace-level bindings and `let` for local bindings - so we are ready
to tackle one of "the only two hard problems in computer science".
footnote:[This is a tongue in cheek reference to Phil Karlton's famous quote,
"There are only two hard things in Computer Science: cache invalidation and
naming things."] Combining this knowledge with what we will learn in the next
few lessons, we will be able to start writing some interesting applications. We
can now:

- Explain what a var is and how it is referred to by a symbol
- Define global bindings using `def`
- Define local bindings using `let`
