(ns errors.err
    (:refer-clojure :exclude [map]))

(defn ok [val]
  [:ok val])

(defn error [val]
  [:error val])

(defn is-ok? [err]
  (= :ok (first err)))

(def is-error? (complement is-ok?))

(def unwrap second)

(defn map [f err]
  (if (is-ok? err)
    (-> err unwrap f ok)
    err))

(defn flat-map [f err]
  (if (is-ok? err)
    (-> err unwrap f)
    err))

(comment
  "The following is an example of using this error type in practice"
  (defn div [x y]
    (if (zero? y)
      (error "Cannot divide by zero")
      (ok (/ x y))))

  (unwrap (div 27 9))

  (unwrap (div 27 0))

  (map #(+ % 12) (div 27 9))

  (unwrap
    (flat-map
        #(div % 2)
        (div 27 9))))
