(ns learn-cljs.import-fns
  (:require learn-cljs.import-fns.ui
            [learn-cljs.import-fns.format :refer [pluralize]]
            [learn-cljs.import-fns.inventory :as inventory]
            [goog.dom
             :refer [getElement]
             :rename {getElement get-element}]))

(defn item-description [i item]
  (let [qty (inventory/item-qty i item)
        label (if (> qty 1) (pluralize item) item)]
    (str qty " " label)))

(let [i (-> (inventory/make-inventory)
            (inventory/add-items "Laser Catapult" 1)
            (inventory/add-items "Antimatter Scrubber" 5))]
  (learn-cljs.import-fns.ui/render-list (get-element "app")
                                        (map (partial item-description i)
                                             (inventory/list-items i))))
