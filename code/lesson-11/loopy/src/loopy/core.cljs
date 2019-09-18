(ns loopy.core
  (:require
   [sablono.core :as sab :include-macros true])
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-doc deftest]]))

(enable-console-print!)

(defcard-doc
  "## `for` Example: Squaring numbers 0-9"

  "### Code:
  ```
  (for [n (range 10)]
    (* n n))
  ```

  ### Result:"
  
  (for [n (range 10)]
    (* n n)))

(defcard-doc
  "## `for` Example: Getting Hypotenuse

   ### Code:
   ```
   (let [sides-list (list [4.2 6] [4 4] [3 4] [5.5 3])]
     (for [sides sides-list]
       (js/Math.sqrt (+ (js/Math.pow (first sides) 2)
                        (js/Math.pow (second sides) 2)))))
  ```
  ### Result:"
  (let [sides-list (list [4.2 6] [4 4] [3 4] [5.5 3])]
    (for [sides sides-list]
      (js/Math.sqrt (+ (js/Math.pow (first sides) 2)
                       (js/Math.pow (second sides) 2))))))

(defcard-doc
  "## `for` Example: Product Combinations

   ### Code:
   ```
   (let [colors [:magenta :chartreuse :taupe]
         sizes [:sm :md :lg :xl]
         styles [:plain :regular :fancy]]
     (for [color colors
           size sizes
           style styles]
       [color size style]))
   ```
   ### Result:"
  (let [colors [:magenta :chartreuse :taupe]
        sizes [:sm :md :lg :xl]
        styles [:plain :regular :fancy]]
    (for [color colors
          size sizes
          style styles]
      [color size style])))

(defcard-doc
  "## `for` Example: Modifiers

   ### Code:
   ```
   (for [n (range 100)
        :let [square (* n n)]
        :when (even? n)
        :while (< n 20)]
    (str \"n is \" n \" and its square is \" square)
   ```
   ### Result:"
  (for [n (range 100)
      :let [square (* n n)]
      :when (even? n)
      :while (< n 20)]
  (str "n is " n " and its square is " square)))

(defcard-doc
  "## `loop`/`recur` Example: GCD

   ### Code:
   ```
   (defn gcd [x y]
     (if (= y 0)
       x
       (gcd y (mod x y))))

   (gcd 90 60)
   ```
   ### Result:"
  (letfn [(gcd [x y]
            (if (= y 0)
              x
              (gcd y (mod x y))))]
    (gcd 90 60))
  "### Code:
   ```
   (loop [x 90, y 60]
     (if (= x 0)
       y
       (recur y (mod x y))))
   ```
   ### Result:"
  (loop [x 90, y 60]
    (if (= y 0)
      x
      (recur y (mod x y)))))

(defcard-doc
  "## `loop`/`recur` Example: Legal and Illegal recur

   ### Code:
   ```
   (loop [i 0
          numbers []]
     (if (= i 10)
       numbers
       (recur (inc i) (conj numbers i))))
   ```
   ### Result:"
  (loop [i 0
         numbers []]
    (if (= i 10)
      numbers
      (recur (inc i) (conj numbers i))))
  "### Code:
   ```
   (loop [i 7
          fact 1]
     (if (= i 1)
       fact
       (* i (recur (dec i) (* i fact)))))
   ```
   ### Result:
   ```
   Can't recur here at line 5
   ```"
)

(defcard-doc
  "## `doseq` Example: Side Effects

   ### Code:
   ```
   (defn send-to-api [user]
     (println \"Sending to API:\" user))

   (let [users [{:name \"Alice\"}
                {:name \"Bob\"}
                {:name \"Carlos\"}]]
     (doseq [user users]
       (send-to-api user)))
   ```
   ### Result:"
  (let [send-to-api #(println "Sending to API:" %)
        users [{:name "Alice"}
               {:name "Bob"}
               {:name "Carlos"}]]
    (doseq [user users]
      (send-to-api user))
    "See devtools console")
)

(first
 (for [i (range 10)]
   (println i)))


(defn main []
  ;; conditionally start the app based on whether the #main-app-area
  ;; node is on the page
  (if-let [node (.getElementById js/document "main-app-area")]
    (.render js/ReactDOM (sab/html [:div "This is working"]) node)))

(main)

;; remember to run lein figwheel and then browse to
;; http://localhost:3449/cards.html

