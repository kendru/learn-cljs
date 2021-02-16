---
title: "Capstone 2 - Temperature Converter | Lesson 15"
date: 2019-10-13T15:01:59+03:00
type: "docs"
---

# Lesson 15: Capstone 2 - Temperature Converter

Over the past few lessons, we have learned the basic concepts that we will need for practically any app that we write: variables for hanging on to values that we want to re-use, control structures for determining which code path should be taken, functions for defining re-usable behavior and calculations, and IO for interacting with the user. While there is still much ground to cover, we can already begin to write useful apps.

---

*In this lesson:*

- Create the structure of an app declaratively in HTML
- Apply our knowledge of basic ClojureScript to create a widget-like app

---

In this lesson, we will build a simple app that will take the temperature and convert it from Celsius to Fahrenheit or vice-versa, depending on the value of a radio button that the user can toggle. For this app, we will need an input for the user to enter a temperature, a couple of radio buttons for them to select the unit of measure that they have entered, and a target to display the converted value. This is what the completed project looks like:

![Complete Temp Converter App](/img/lesson15/temp-converter-screenshot.png)

_Complete Temp Converter App_

First, we will create the Figwheel project:

```shell
$ clj -X:new :template figwheel-main :name learn-cljs/temp-converter :args '["+deps"]'
```

Again, since we will not be writing automated tests, remove the `"test"` entry from `:watch-dirs` in `dev.cljs.edn`.

## Creating the Markup

In the last chapter, we manually built up the DOM, creating and appending elements in code. Besides being cumbersome to work with, this is not very idiomatic ClojureScript. Remember that we should favor _declarative_ apps over _imperative_ ones. This time, we will define our entire markup in HTML. Replace the `app` tag in the generated `index.html` with the following.

```html
<div id="app">
  <h1>Temp Converter</h1>

  <div class="user-input">
    <div class="unit-control">                             <!-- 1 -->
      <p>Convert from</p>
      <div class="radio-option">
        <input type="radio" id="unit-c" name="unit" value="c" checked="checked" />
        <label for="unit-c">Celsius</label>
      </div>
      <div class="radio-option">
        <input type="radio" id="unit-f" name="unit" value="f" />
        <label for="unit-f">Fahrenheit</label>
      </div>
    </div>

    <div class="temp-control">                             <!-- 2 -->
      <label for="temp">Temperature:</label>
      <input type="number" id="temp" name="temp" />
    </div>
  </div>

  <div class="converted-output">                           <!-- 3 -->
    <h3>Converted Value</h3>

    <span id="temp-out">-</span>
    <span id="unit-out">F</span>
  </div>
</div>
```

_Temperature Converter Markup_

1. Radio buttons used to switch between units
2. Text input for the user to enter a temperature
3. Result display area

This markup defines all of the elements that we will use in our app, so we do not need to worry about creating any ad-hoc DOM elements in our code - we'll only deal with manipulating the elements that we have already defined. Notice that we have given each element that we will be interacting with a unique `id` attribute so that we can easily get a reference to them using the `goog.dom/getElement` function.

### Quick Review

- What is the advantage of structuring an app declaratively?
- Before we write any ClojureScript code, list out the steps that are necessary to turn this static markup into an application

## Code Walkthrough

Now we will write the ClojureScript code that will interact with the webpage we just created and handle the business logic of converting temperatures. To begin with, we will import the Google Closure libraries that we have been using over the past few lessons: `goog.dom` for DOM manipulation and `goog.events` for reacting to user input:

```clojure
(ns learn-cljs.temp-converter
  (:require [goog.dom :as gdom]
            [goog.events :as gevents]))
```

ClojureScript is often written in a bottom-up fashion, in which we define the low-level operations in our domain first and develop more complex logic by combining the low-level operations. In this case, our domain is very simple, but we will still define the business logic of converting temperatures first:

```clojure
(defn f->c [deg-f]
  (/ (- deg-f 32) 1.8))

(defn c->f [deg-c]
  (+ (* deg-c 1.8) 32))
```

Next, we want to get references to the important elements on the page. We will use Google Closure to find DOM elements on the page and bind each element to a var. This helps keeping the rest of the code clearer, and it helps with performance, since we do not have the overhead of searching the DOM every time we want to use one of these elements.

```clojure
(def celsius-radio (gdom/getElement "unit-c"))
(def fahrenheit-radio (gdom/getElement "unit-f"))
(def temp-input (gdom/getElement "temp"))
(def output-target (gdom/getElement "temp-out"))
(def output-unit-target (gdom/getElement "unit-out"))
```

Next, we will create a few functions that we will use in our event handling code. As with any programming language, factoring each piece of logic into its own function is considered good practice.

```clojure
(defn get-input-unit []
  (if (.-checked celsius-radio)
    :celsius
    :fahrenheit))

(defn get-input-temp []
  (js/parseInt (.-value temp-input)))

(defn set-output-temp [temp]
  (gdom/setTextContent output-target
                       (.toFixed temp 2)))
```

This code should look familiar, as we are dealing with the sort of DOM manipulation that we have been performing over the past few lessons. The `get-input-unit` and `get-input-temp` functions get the unit of measure and temperature to convert respectively, and the `set-output-temp` function updates the display element with the converted temperature.

We will also need a function that we will use as an event handler any time anything changes that will get the currently selected unit of measure and temperature and will update the results section with the converted temperature.

```clojure
(defn update-output [_]
  (if (= :celsius (get-input-unit))
    (do (set-output-temp (c->f (get-input-temp)))
        (gdom/setTextContent output-unit-target "F"))
    (do (set-output-temp (f->c (get-input-temp)))
        (gdom/setTextContent output-unit-target "C"))))
```

This function is the core of our app. It handles each event and updates the UI accordingly. This function will be called with an event object as an argument, but we follow a common convention of using an underscore to name any parameter that we do not use. The other thing to note about this code is that it uses `do` to group several expressions together. `do` takes multiple expression, evaluates all of them in order, and it evaluates to the value of the last expression. Thus, the expression, `(do x y z)`, would evaluate `x` then `y` then `z`, and the entire expression would have the same value as just `z`. This is useful if `x` and `y` have side effects (in our case, updating DOM elements), but we do not care what they evaluate to.

### You Try It

- Add a button that will clear the temperature input

Finally, we will connect our logic to the UI by attaching the `update-output` function as an event handler whenever either radio button is clicked or the input is updated. This will ensure that any time the user changes anything that may affect the converted output, we recalculate the results.

```clojure
(gevents/listen temp-input "keyup" update-output)
(gevents/listen celsius-radio "click" update-output)
(gevents/listen fahrenheit-radio "click" update-output)
```

There, in roughly 40 lines of code, we have a useful ClojureScript app! For the
sake of completeness, the entire code is printed below:

```clojure
(ns learn-cljs.temp-converter
  (:require [goog.dom :as gdom]                            ;; <1>
            [goog.events :as gevents]))

(defn f->c [deg-f]                                         ;; <2>
  (/ (- deg-f 32) 1.8))

(defn c->f [deg-c]
  (+ (* deg-c 1.8) 32))

(def celsius-radio (gdom/getElement "unit-c"))              ;; <3>
(def fahrenheit-radio (gdom/getElement "unit-f"))
(def temp-input (gdom/getElement "temp"))
(def output-target (gdom/getElement "temp-out"))
(def output-unit-target (gdom/getElement "unit-out"))

(defn get-input-unit []                                     ;; <4>
  (if (.-checked celsius-radio)
    :celsius
    :fahrenheit))

(defn get-input-temp []
  (js/parseInt (.-value temp-input)))

(defn set-output-temp [temp]
  (gdom/setTextContent output-target
                       (.toFixed temp 2)))

(defn update-output [_]                                    ;; <5>
  (if (= :celsius (get-input-unit))
    (do (set-output-temp (c->f (get-input-temp)))
        (gdom/setTextContent output-unit-target "F"))
    (do (set-output-temp (f->c (get-input-temp)))
        (gdom/setTextContent output-unit-target "C"))))

(gevents/listen temp-input "keyup" update-output)          ;; <6>
(gevents/listen celsius-radio "click" update-output)
(gevents/listen fahrenheit-radio "click" update-output)
```

_src/learn_cljs/temp_converter.cljs_

1. Require the Google Closure modules needed for this app
2. Define conversion functions
3. Store each element that we will use in a var
4. Helper functions
5. Event handling callback
6. Attach event handler to `keyup` event in the temperature input and the click event on each radio button

### Challenge

This is a very simple app, and it could be extended quite easily. Here are a couple of options for new features that you can add:

- Allow the user to select Kelvin as well, and perform the appropriate conversions.
- Every time the code is reloaded, it will attach more event handlers. Use the initialization pattern discussed in [Lesson 6](/section-1/lesson-6-receiving-rapid-feedback-with-figwheel/#defonce) to ensure that the event handlers are attached only once.

## Summary

We will continue to acquire new building blocks that we will be able to combine with what we have just learned to create more useful and interesting applications. By now, we have a good feel for how ClojureScript is structured, but so far we have not done much that showcases ClojureScript's advantages over JavaScript. That is about to change, as we begin to explore the areas that make ClojureScript unique. In the next lessons, we will look at the rich collection data types that make ClojureScript so productive.
