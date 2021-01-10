(ns notes.errors
  "This namespace comes from Lesson 24: Handling Exceptions and Errors."
  (:refer-clojure :exclude [map]))

(defn ok [val]
  [:ok val])

(defn error [val]
  [:error val])

(defn is-ok? [err]
  (= :ok (first err)))

(def is-error? (complement is-ok?))

(def unwrap second)

(defn unwrap-or [on-error err]
  (if (is-ok? err)
    (unwrap err)
    (on-error (unwrap err))))

(defn map [f err]
  (if (is-ok? err)
    (-> err unwrap f ok)
    err))

(defn flat-map [f err]
  (if (is-ok? err)
    (-> err unwrap f)
    err))
