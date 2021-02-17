(ns learn-cljs.reagent-test
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]
            [goog.dom :as gdom]
            [goog.events :as gevents]))

(def a-cell (r/atom 0))
(def b-cell (r/atom 0))
(def c-cell
  (ratom/make-reaction
   #(+ @a-cell @b-cell)))

(def a (gdom/getElement "cell-a"))
(def b (gdom/getElement "cell-b"))
(def c (gdom/getElement "cell-c"))

(defn update-cell [cell]
  (fn [e]
    (let [num (js/parseInt (.. e -target -value))]
      (reset! cell num))))

(gevents/listen a "change" (update-cell a-cell))
(gevents/listen b "change" (update-cell b-cell))

(ratom/run!
 (set! (.-value c) @c-cell))
