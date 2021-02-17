(ns learn-cljs.chat.components.render-helpers
  (:require [clojure.string :as s]))

(defn display-name [person]
  (if person
    (->> person
        ((juxt :first-name :last-name))
        (s/join " "))
    "REMOVED"))
