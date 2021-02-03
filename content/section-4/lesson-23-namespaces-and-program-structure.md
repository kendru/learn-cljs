---
title: "Namespaces and Program Structure | Lesson 23"
date: 2019-11-20T21:48:49-07:00
type: "docs"
---

# Lesson 23: Namespaces and Program Structure

In ClojureScript, the unit of modularity is the namespace. A namespace is simply a
logical grouping of functions and data that can be required and used by another
namespace. External libraries expose modules as namespaces that we can require in our
code, and we can (and should) break our code into multiple modules as well. In this lesson,
we'll learn how to declare and require modules as well as how to use them to create
clean architectural boundaries in our programs.

---

*In this lesson:*

- Organize functions and data into namespaces
- Architect a logical and intuitive project structure
- Work with namespaces at the REPL

---

## Namespace Declarations

In each of the ClojureScript files that we have written so far, Leiningen has kindly generated a namespace declaration at the top of the file for us. We have used the `(:require ...)` form to
pull in third-party libraries, but we have judiciously avoided diving in to what exactly is
happening here. That is about to change.

For the purpose of exploring namespaces in this lesson, let's create a new Figwheel
project:

```
$ lein new figwheel import-fns
$ cd import-fns
```

If we look at the file that was generated in `src/import_fns/core.cljs`, we see the
following declaration at the top of the file:

```clojure
(ns import-fns.core
    (:require ))
```

The ClojureScript compiler will turn this declaration into a Google Closure Compiler module
declaration, which will ultimately be compiled into a JavaScript object containing all of the
functions and vars that we define in this namespace. From this bare-bones declaration, we can
note a couple of things:

1. We declare the name of the namespace as `import-fns.core`. This name must follow the
naming convention discussed in [Lesson 5](/section-1/lesson-5-bootstrapping-a-clojurescript-project/#exploring-the-project).
2. There is a skeleton `(:require ...)` form that we can use to require code from other
namespaces into this one.

### Using Require

Since declaring a name is fairly self-explanatory, we will focus on the second item -
requiring code from other namespaces. The `:require` form in the `ns` declaration is
by far the most common way to require code from another file - including third party
libraries - to make it available in our code[^1]. Let's look at the various forms of
`:require`, using the project that we created above. We will again build on the example
of a text-based adventure game, specifically displaying a player's inventory:

```clojure
(ns import-fns.core
    (:require import-fns.ui                                ;; <1>
              [import-fns.format :refer [pluralize]]       ;; <2>
              [import-fns.inventory :as inventory]         ;; <3>
              [goog.dom :refer [getElement]                ;; <4>
                        :rename {getElement get-element}]))

(defn item-description [i item]
  (let [qty (inventory/item-qty i item)
        label (if (> qty 1) (pluralize item) item)]
      (str qty " " label)))

(let [i (-> (inventory/make-inventory)
            (inventory/add-items "Laser Catapult" 1)
            (inventory/add-items "Antimatter Scrubber" 5))]
  (import-fns.ui/render-list (get-element "app")
    (map (partial item-description i)
         (inventory/list-items i))))
```

1. Require the entire `import-fns.ui` namespace
2. Require a single function from the `import-fns.format` namespace
3. Require the `import-fns.inventory` namespace with the alias `inventory`
5. Require a single function from the Google Closure Library namespace, renaming that function

This snippet illustrates all of the common ways of requiring code into a namespace. After the
`:require` keyword, we can include any number of _libspecs_. The libspec may be either a
fully-qualified namespace or a vector containing the namespace and some optional modifiers.
This syntax is interpreted by the `ns` special form to pull in vars from other namespaces
according to the exact form of the libspec.

#### Form 1: Simple Namespace

In the first form, we simply use the name of a namespace that we would like to require:
`import-fns.ui`. We can then refer to any public var within this namespace by using the
full namespace followed by `/` and the name of the var, e.g. `import-fns.ui/render-list`.

#### Form 2: Refer

In the second case, we follow the namespace by `:refer [pluralize]`. `:refer` will make every
var that is listed in the vector that follows available without any prefix. Thus, we can write
`(pluralize item)` instead of `(import-fns.format/pluralize item)`. Tny function in the
`import-fns.format` namespace can still be called using the fully-qualified syntax.

#### Form 3: Aliased Namespace

In the third case, we alias the namespace that we require using `:as`. This works similar to
the first case in that we are able to refer to any public var in the namespace. However, we can
use the symbol that we specified after `:as` instead of the full namespace. Thus,
`import-fns.inventory/make-inventory` becomes `inventory/make-inventory`. The use of `:as`
can be very useful when requiring from namespaces with very verbose names. In general, we
should also prefer `:as` to `:refer`, since it makes it clear where a var that we reference
comes from without needing to look back at the namespace declaration.

#### Form 4: Aliased Vars

Finally, in the fourth case, we rename a specific var that we referred. Here, we alias the
`getElement` function from the Google Closure Library's `goog.dom` module in order to give
it a more idiomatic `kebab-case` name. In practice, renaming is used more often to prevent
a name collision. For example, we may want to import functions called `mark-inactive` from
both `my-app.users` and `my-app.customers` namespaces. We can use `:rename` to import them
as `mark-user-inactive` and `mark-customer-inactive`.

| Require Form | Description | Function Usage |
| ------------ | ----------- | ----- |
| `my.namespace` | require entire namespace | `my.namespace/compute` |
| `my.namespace :refer [compute other-fn]` | require specific functions and refer to them unqualified | `compute` |
| `my.namespace :as mine` | require namespace with alias | `mine/compute` |
| `my.namespace :refer [compute] :rename {compute calculate}` | require namespace with specific vars aliased | `calculate` |

_Forms of Require_

> **NOTE**
>
> There is a form, `:refer-clojure` that allows us to rename a var from the ClojureScript standard
> library (or exclude it altogether) in the event that we want to define a var in our namespace
> with the same name. `(:refer-clojure :rename {str string})` will alias the `str` function as
> `string`, so that we are free to reuse the `str` name without generating any warnings. Similarly,
> we can exclude the `str` function altogether with, `(:refer-clojure :exclude [str])`. That said,
> we should usually be giving things names that are descriptive enough that they do not conflict
> with anything in the standard library.

### Importing Google Closure Library Classes

In the case that we are importing a class constructor from the Google Closure Library, there is one
form that we should be aware of in addition to `:require` - that is `:import`. This form exists only
for bringing a class constructor from a Google Closure module into our namespace in the
same way that the `:refer` keyword in a libspec lets us bring in a var from another namespace. Like
`:require`, `:import` expects to be followed by a number of import specs. The most common form is a
vector with the namespace from which to import classes followed by the unqualified constructor names
to import from that namespace:

```clojure
(ns my-ns
  (:import [goog.math Coordinate Rect]))

(.contains (Rect. 10 50 5 5)
           (Coordinate. 12 50))
```

The thing to remember about `:import` is that it is _only_ used for requiring classes (including enums)
from Google Closure modules - never for including a ClojureScript namespace or a (non-constructor)
function from a Google Closure module.

### Requiring Macros

One of ClojureScript's most powerful features is its macro support. However, the way in which macros are
implemented presents a bit of a quirk in the way that we have to require them. A macro has access to the
ClojureScript code that is "inside" its call site as raw data (lists, vectors, symbols, etc.), and it
can manipulate this code as data structure before returning the code that actually gets compiled to
JavaScript. The tricky thing is that since the ClojureScript compiler is written in Clojure, the macros
themselves are _Clojure_ code that generates _ClojureScript_ code. We will learn more about macros in a
later lesson, but for now the important thing to remember is that since macros are Clojure code, we
need to go about requiring them another way. That way is the `:require-macros` form:

```clojure
(:require-macros [macro-ns :as macros])
```

This form functions very much like `:require` with the exception that it works with importing macros
from Clojure namespaces rather than vars from ClojureScript namespaces.

![Macro Compilation](/img/lesson23/macro-compilation.png)

_Macro Compilation_

## Grouping Related Functions

Now that we have discussed the mechanics of declaring a namespace and requiring other code that we would
like to use, let's take a step back and look at how to organize our namespaces. One of the features of
ClojureScript that can be both liberating and frustrating is that it is not opinionated about how
namespaces are structured. We can put as little or as much into a namespace as we would like, and as long
as we do not create circular dependencies, all is well. Some of the insights from object-oriented
programming can also help us here: high cohesion and low coupling.

High cohesion means that if two pieces of code are closely related in purpose, they should be close
in the architecture of the program - usually in the same namespace. Low coupling is the other side of
the same coin: if two pieces of code are unrelated, they should be distant from each other in the
architecture of the program. Unlike OOP practitioners, we extend this principle to data as well. Since
hiding data is not a goal of functional programming, we tend to group functions and the data that they
operate on into the same namespace and expose both freely.

Let's return to the adventure game inventory code by way of example. In the `import-fns.core` namespace,
we require the `import-fns.inventory` and `import-fns.ui` namespaces as well as `pluralize` from
`import-fns.format`. These files contain the business and presentation logic for this little app. First,
we will look at `import-fns.inventory`:

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

As the name suggests, this namespace contains all of the code relating to creating and
managing an inventory. It has no formatting or display logic. We expose `make-inventory`
for constructing a new inventory, `add-items` for adding some quantity of a specific item,
`list-items` for getting the distinct items in the inventory, and `item-qty` for getting
the quantity of a particular item. We make the `add-quantity` function private by declaring
it with `defn-` because it exists solely as an implementation detail of `add-items` and
serves little value outside this context. Next, let's look at the `import-fns.format` namespace:

```clojure
(ns import-fns.format
  (:require [clojure.string :as s]))

(defn ends-with-any? [word suffixes]
  (some (fn [suffix]
          (s/ends-with? word suffix)
         suffixes)))

(defn replace-suffix [word old-suffix new-suffix]
  (let [prefix-len (- (count word)
                      (count old-suffix))
        prefix (.substring word 0 prefix-len)]
    (str prefix new-suffix)))

(defn pluralize [word]
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

Again, this namespace contains all of the string formatting functions that we need for the
app. Even though we only require `pluralize` in our main namespace, we make all of the functions
public since they all contain reusable logic. Additionally, when we expose every function, we
make it possible to test every function in isolation. Note that all of these function are general
functions that will work with any string. If we had code that was specific to inventories, we
would put it in the `inventory` namespace. So what should we do if we have a set of functions that
are formatting functions specific to inventories? One common practice is to create a new namespace
called something like `import-fns.format-inventory`, and this namespace could require functions and
data from both formatting and inventory namespaces to perform its specialized work. When we do not
have to squeeze the architecture of our code into a class hierarchy, it gives us a great deal of
flexibility. Finally, for the sake of completeness, let's look at the `import-fns.ui` namespace:

```clojure
(ns import-fns.ui
  (:require [goog.dom :as gdom]))

(defn append-list-item [list text]
  (gdom/appendChild list
    (let [li (gdom/createElement "li")]
      (gdom/setTextContent li text)
      li)))

(defn render-list [elem items]
  (let [ul (gdom/createElement "ul")]
    (doseq [item items]
      (append-list-item ul item))
    (gdom/removeChildren elem)
    (gdom/appendChild elem ul)))
```

_src/import\_fns/ui.cljs_

There is not much that is novel to note about this namespace, except that its API is formed
around rendering text rather than an inventory specifically. The "glue code" that ties
inventories and rendering together is all in our core namespace.

### Avoiding “Hidden” OOP

One thing that many new ClojureScript developers will realize is that with atoms and namespaces
that can contain private variables, we can easily emulate stateful objects. This is true - we
can define a private var to hold our object state within an atom, then we can use public functions
to define the API and private functions as implementation details. However, as soon as we introduce
hidden state, we lose the benefits of functional purity and referential transparency in terms
of both testability and reasonability.

## Namespaces and the REPL

Before moving on, let's look briefly at how to interact with namespaces from the REPL. When we
used `:require` or `:import` in the namespace declaration, the `ns` special form used these
directives to determine what code to make available within our namespace. There are macros that
act as analogs to these directives - `require` and `import` - which we can run from the REPL to
expose code to our REPL session:

```clojure
dev:cljs.user=> (require '[import-fns.format :as fmt] :reload)
nil
dev:cljs.user=> (fmt/pluralize "burrito")
"burritos"
```

The primary difference is that we must quote the vector that we pass to `require` -
otherwise, the REPL would try to resolve the `import-fns.format` and `fmt` symbols to vars that
are bound in our current namespace. Quoting the vector (i.e. using `'[]` instead of `[]`) causes
ClojureScript to interpret each symbol in it as a literal symbol rather than attempting to resolve
them. An additional difference is that we can add the `:reload` keyword after the require spec to
cause the REPL to reevaluate the namespace in order to pick up on any changes that were made to
the file since we last required it.

By default, when we start up a REPL, it operates inside a namespace called `cljs.user`. Anything
that we `def` (or `defn`) will be defined inside this namespace for the duration of the REPL
session. If we would like to operate inside another namespace, we can use the `in-ns` special
form to instruct the REPL to operate within another namespace instead:

```clojure
dev:cljs.user=> (in-ns 'import-fns.format)

dev:import-fns.format=> (replace-suffix "programmer" "er" "ing")
"programming"
```

Using `in-ns` can simplify our workflow when we are practicing REPL-driven development. We can
start writing our code for a namespace in the REPL then copy all of our definitions to the
file when we are satisfied with how they work.

## Summary

In this lesson, we learned what namespaces are as well as how to declare a namespace with
its dependencies using `:require` and `:import`. We also looked at how to think about
organizing our code into namespaces using the principles of high cohesion and low coupling.
Finally, we looked at how to interact with namespaces from the REPL so that we can effectively
navigate and test our code.

[^1]: There is another form, `(:use [namespace :only [var1 var2]])` that works just like `(:require [namespace :refer [var1 var2]])`. It used to be used heavily in Clojure before `:refer` was available inside `(:require)`, but it has all but fallen out of use.