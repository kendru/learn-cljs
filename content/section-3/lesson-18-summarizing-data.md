---
title: "Summarizing Data | Lesson 18"
date: 2019-10-22T10:54:24+03:00
type: "docs"
---

# Lesson 18: Summarizing Data

So far we have learned some useful operations that we can perform over sequences:

- `map` for transforming each element
- `filter` for selecting certain elements
- `into` for converting between collection types

There is a lot that we can do with just these few functions. In fact, we rarely need to use something like a `for` loop with these functions at our disposal. In the last lesson, we used these functions to calculate sales tax for items in a shopping cart and filter to only the taxable items. However, consider the scenario where we would like to get the total price of the cart or the average value of items in the cart. In this case, we cannot use map - we want a single value, and map always evaluates to another sequence the same size as the original. Filter is also obviously not what we are looking for. Is there some way to get "summary" data from out of a sequence without resorting to `for` loops? There are indeed other options, the most common of which is the `reduce` function.

---

**In this lesson:**

- Aggregating values over an entire sequence
- Writing reducing functions
- Simplifying recursive code with `reduce`

---

### Exercise: Getting cart value

Let's take the same general shopping cart model from the last lesson and try to find the total value of every item in the cart. We will again consider an example of how we could do this imperatively in JavaScript. This time, we are going on a space journey and need to pick up a couple of supplies that we may need on the way:

```javascript
const cart = [                                             // <1>
    { name: "Tachyon Emitter Array", price: 1099.45 },
    { name: "Dilithium Matrix", price: 2442.00 },
    { name: "Antimatter Chamber Sealant Rings (4)", price: 19.45 },
    { name: "Toothbrushes (2-pack)", price: 8.50 }
];

let total = 0;                                             // <2>
for (let item of cart) {
    total += item.price;
}

console.log(total.toFixed(2)); // "3569.40"
```

_Imperative Shopping Cart Value_

1. Use the same cart data structure that we used in [the previous lesson](/section-3/lesson-17-discovering-sequence-operations/)
2. Calculate a running total with a `for` loop

The pattern here is easy to see: we have some value - in this case the variable, `total` that is updated for every element in the `cart` array. We see and use code like this on a daily basis, so it is not hard to figure out what is going on, but there is unnecessary complexity in the for loop. In ClojureScript, the pattern that we will use is a `reduce`.

```clojure
cljs.user=> (def cart                                      ;; <1>
              [{:name "Tachyon Emitter Array" :price 1099.45}
               {:name "Dilithium Matrix" :price 2442.00}
               {:name "Antimatter Chamber Sealant Rings (4)" :price 19.45}
               {:name "Toothbrushes (2-pack)" :price 8.50}])
#'cljs.user/cart

cljs.user=> (defn add-price [total item]                   ;; <2>
              (+ total (:price item)))
#'cljs.user/add-price

cljs.user=> (def total (reduce add-price 0 cart))          ;; <3>
#'cljs.user/total

cljs.user=> (.toFixed total 2)
"3569.40"
```

1. Define the cart data structure
2. Create a reducing function that takes a running total and a new element and yields a new total
3. Apply this reducing function to the entire cart to extract the total

## Understanding Reduce

While the workings of `map` and `filter` were evident just looking at some code that uses them, we need to dig a little deeper with `reduce`. Like `map` and `filter`, it takes a function and a sequence and returns _something_. However, `reduce` also takes an extra parameter, and the function that it takes looks a little different than the functions that we passed to `map` and `filter`. For one thing, this function takes two parameters instead of just one. It also does not always return a sequence. So then, how does `reduce` work?

Reduce commonly takes 3 parameters: an initial value, a reducing function, and a sequence. It first evaluates the reducing function with the initial value and the first element of the sequence as its arguments and takes the evaluated value and passes it into the reducing function again, along with the second item of the collection. It continues evaluating the reducing function for each element in the sequence, feeding the resulting value back into the next call. Finally, `reduce` itself evaluates to whatever the final call to the reducing function yields. This it quite a lot to wrap our heads around, so let's use a diagram to help us understand visually what is going on.

![Reducing a Sequence](/img/lesson18/reduce-over-sequence.png)

We can see that reduce keeps building up a value over each evaluation of the reducing function. On the first call, it added the initial value, `0` and the first element of the sequence, `5`, to get `5`. It then added this value and the next element in the sequence, `10`, to get `15`. It proceeded through the entire sequence in this manner and yielded the last value, `25`, as the final result. Notice that we were able to take a function that does not know how to operate on a sequence (`+`) and somehow applied it to get the sum of all of the numbers in a sequence.

Imagine children packing snowballs for a snowball fight: they hold a little bit of snow in one hand and pack on more snow with their other hand. Every time they pack on more snow, the snowball grows larger and larger, until the snowball is to their liking. Reduce operates in a very similar way, "accumulating" more and more data with each pass. For this reason, the first parameter that is passed to the reducing function is often called an _accumulator_ or a _memo_.

### Quick Review

Given the following code:

```clojure
;; Create a seq of words
(def words (clojure.string/split
            "it was the best of times it was the worst of times"
            #" "))

(defn count-words [counts word]
  (update-in counts [word] #(inc (or % 0))))

(def word-counts (reduce count-words {} words))
```

- What is the data type of `word-counts`?
- What is the value of `word-counts`?

Before trying this code out at the REPL, use a pencil and paper to write out the calls that will be made to the `count-words` function. If you need to look up documentation on a specific function that is unfamiliar, you can either call `(doc function-name)` from the REPL or look up the function on https://clojuredocs.org.

## Reduce Use Cases

Reduce is the go-to tool for any case when:

- We need to accumulate state while walking over a sequence, and
- There is not an existing function in the standard library that does
what we want

Imagine that our app tracks user events on a webpage for analytics purposes. We have a list of events that are modeled as a map with the `:event` that the user performed and the `:timestamp` at which the event took place:

```clojure
(def events [{:event :click, :timestamp 1463889739}
             {:event :typing, :timestamp 1463889745}
             {:event :click, :timestamp 1463889746}
             {:event :click, :timestamp 1463889753}])
```

Our task is to figure out the longest interval during which the user was idle. To do this, we need to keep track of the longest time between events that we have seen so far and the last timestamp that we have seen. Since there are multiple items that we want to keep track of, we can use a map as the accumulator:

```clojure
(defn longest-idle-time [events]
  (:max-idle                                               ;; <1>
    (reduce (fn [{:keys [max-idle last-ts]} event]         ;; <2>
              (let [ts (:timestamp event)
                    idle-time (- ts last-ts)]
                {:max-idle (max max-idle idle-time)        ;; <3>
                 :last-ts ts}))
            {:max-idle 0
             :last-ts (:timestamp (first events))}         ;; <4>
            events))))
```

1. Since `reduce` will return a map, retrieve only a single value
2. Define the reducing function inline
3. Return a map from the reducing function
4. Define the initial value as a map

In this case, we want to know the longest interval between events in seconds, but we also need to keep track of when the previous event occurred so that we can calculate the new interval time. Using a map as the accumulator allows us to keep track of as many pieces of state as we need, and as a final step we get only the specific value that we are interested in, discarding the intermediate results that we no longer need.

### You Try It

- Write a function that returns how many times the user clicked something.
- Write a function that takes a sequence of events and tells us how many times the user double-clicked, where a double-click is defined as two clicks within the same timestamp. You may assume that the list of events is ordered by timestamp ascending.

## Being More Concise

Reduce is a handy function that helps write clear and expressive code, but there are a couple things that we should keep in mind in order to write even more concise code. First, there is a 2-argument version of reduce that we can use in quite a few circumstances:

```clojure
(reduce reducing-fn vals)
```

In this case, we do not supply an initial value to pass to `reducing-fn`. It will instead be called initially with the first two elements in `vals`. This works very well in cases where we are doing something like summing numbers:

```clojure
cljs.user=> (reduce + [6 7 8])
21

;; (+ 6 7)  => 13
;; (+ 13 8) => 21
```

Besides saving a few keystrokes, this 2-argument version of `reduce` is easier to read.

The second tip for making reduce more concise is to try to map or filter values before reducing them. Going back to the shopping list example, if we wanted to get the subtotal of the price of all taxable items, we _could_ perform everything in a single `reduce`:

```clojure
(reduce (fn [total item]
          (if (:taxable? item)
            (+ total (:price item))))
        0
        cart)
```

However, it is usually clearer to have transformation, filtering, and reducing done as separate steps:

```clojure
(reduce +                                                  ;; <1>
        (map :price                                        ;; <2>
             (filter :taxable? cart)))                     ;; <3>
```

1. Sum all values
2. Of the prices
3. Of taxable items

With `map`, `filter`, and `reduce` split into their own separate steps, the intention of this code is crystal clear, and it is _simple_. Each concern is tightly encapsulated, and no piece of code is trying to do too much. Additionally, each piece is more re-usable than if we had put all of the logic in a single reducing function.

### Challenge

Reduce is a much more general function than `map` or `filter` in fact, `map` and `filter` can both be implemented using `reduce`. Here is an implementation of `map`:

```clojure
(defn my-map [xform vals]
  (reduce (fn [new-vals elem]
            (conj new-vals (xform elem)))
            '()
            (reverse vals)))
```

Following a similar pattern, implement your own version of `filter`, and call it `my-filter`.

## Summary

Let's consider what we can now do with reduce:

- Keep a running total over a sequence
- Re-write much recursive code more succinctly
- Implement any sequence operation as a reduce

In this lesson, we covered the fundamental `reduce` function, and we applied it to the problem of summing the price of every item in a shopping cart. Because of its generality and its ability to simplify recursive code, `reduce` is quite common in ClojureScript code. Now that we know how to work with sequences, we will turn our attention to modeling a domain with ClojureScript collections.
