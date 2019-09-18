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
  (let [scene (get game current)
        type (:type scene)]
    (io/clear term)
    (when (or (= :win type)
              (= :lose type))
      (io/print term
                (if (= :win type) "You've Won: " "Game Over: ")))
    (io/println term (:title scene))
    (io/println term (:dialog scene))
    (io/read term #(on-answer game current %))))

(defn on-answer [game current answer]
  (let [scene (get game current)]
    (if (= :skip (:type scene))
      (:on-continue scene)
      (condp = answer
        "reset" (prompt game :start)
        "help" (do (io/clear term)
                   (io/println term "Valid commands:")
                   (io/println term "\t- %s: %s" "reset" "Return to the beginning of the game")
                   (io/println term "\t- %s: %s" "help" "Display this message")
                   (io/println term "\t- %s: %s" "yes" "Answer in the affirmative")
                   (io/println term "\t- %s: %s" "no" "Answer in the negative")
                   (io/println term "Press enter to continue.")
                   (io/read term #(prompt game current)))
        "yes" (prompt game (get-in scene [:transitions "yes"]))
        "no" (prompt game (get-in scene [:transitions "no"]))
        (do (io/clear term)
            (io/println term "I didn't understand that. You can enter \"help\" for assistance.")
            (io/println term "Press enter to continue.")
            (io/read term #(prompt game current)))))))

(prompt data/game :start)


