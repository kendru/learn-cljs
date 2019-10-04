(ns import-fns.format
  (:require [clojure.string :as s]))

(defn- ends-with-any? [word suffixes]
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
