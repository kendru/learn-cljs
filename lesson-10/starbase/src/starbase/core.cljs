(ns starbase.core
  (:require [bterm.core :as bterm]
            [bterm.io :as io]
            [starbase.data :as data]))

(enable-console-print!)

(def term
  (bterm/attach (.getElementById js/document "terminal")
                {:prompt "=> "
                 :font-size 14}))

(declare on-answer)

(defn prompt [game current]
  )

(defn on-answer [game current answer]
   )

(prompt data/game :start)


