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
