(ns import-fns.core
    (:require import-fns.ui
              [import-fns.format :refer [pluralize]]
              [import-fns.inventory :as inventory]
              [goog.dom :refer [getElement]
                        :rename {getElement get-element}]))

(defn item-description [i item]
  (let [qty (inventory/item-qty i item)
        label (if (> qty 1) (pluralize item) item)]
      (str qty " " label)))

(let [i (-> (inventory/make-inventory)
            (inventory/add-items "Laser Catapult" 1)
            (inventory/add-items "Antimatter Scrubber" 5))]
  (import-fns.ui/render-list (get-element "app")
    (map (partial item-description i)
         (inventory/list-items i))))

