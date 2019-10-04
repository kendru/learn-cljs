---
title: "Namespaces and Program Structure | Lesson 25"
type: "docs"
date: 2019-10-01T21:43:00-06:00
---


In the example below,
we declare functions in the `import-fns.inventory` and `import-fns.format` namespaces and require
them in the `import-fns.core` namespace:

```clojure
(ns import-fns.inventory)                                  ;; <1>

(defn make-inventory []                                    ;; <2>
  {:items {}})

(defn- add-quantity [inventory-item qty]                   ;; <3>
  (update-in inventory-item [:qty]
    (fn [current-qty] (+ current-qty qty))))

(defn add-items
 ([inventory item] (add-items inventory item 1))
 ([inventory item qty]
  (update-in inventory [:items item]
    (fnil add-quantity
      {:item item :qty 0}
      qty))))

(defn list-items [inventory]
  (keys (:items inventory)))

(defn item-qty [inventory item]
  (get-in inventory [:items item :qty] 0))
```

_src/import\_fns/inventory.cljs_

1. All functions in this file will be part of the `import-fns.inventory` namespace
2. Declare a public function named `make-inventory`
3. Use `defn-` to declare a private function

Next, let's look at the formatting functions:

```clojure
(ns import-fns.format
  (:require [clojure.string :as s]))                       ;; <1>

(defn- ends-with-any? [word suffixes]                      ;; <2>
  (some (fn [suffix]
          (s/ends-with? word suffix))
        suffixes))

(defn replace-suffix [word old-suffix new-suffix]          ;; <3>
  (let [prefix-len (- (count word)
                      (count old-suffix))
        prefix (.substring word 0 prefix-len)]
    (str prefix new-suffix)))

(defn pluralize [word]                                     ;; <4>
  (cond
    (ends-with-any? word ["s" "sh" "ch" "x" "z"])
    (str word "es")

    (s/ends-with? word "f")
    (replace-suffix word "f" "ves")

    (s/ends-with? word "fe")
    (replace-suffix word "fe" "ves")

    (re-find #"[^aeiou]y$" word)
    (replace-suffix word "y" "ies")

    :else (str word "s")))
```

_src/import\_fns/format.cljs_

1. Require a namespace from the standard library
2. Make `ends-with-any?` a private function because it is only intended
as an implementation detail of `pluralize`.
3. Leave `replace-suffix` as public because it might be a useful formatting function itself.
4. `pluralize` is the main function that we will consume. It pluralizes a subset of English words.

Now, we can use a `:require` clause in another namespace