(ns import-fns.core
    (:require [import-fns.format :as fmt]
              [import-fns.inventory :as inventory]))


(enable-console-print!)

(let [i (inventory/make-inventory)
      i (inventory/add-items i "Laser Catapult" 1)
      i (inventory/add-items i "Antimatter Scrubber" 5)]
  (println "You have:")
  (doseq [item (inventory/list-items i)]
    (let [qty (inventory/item-qty i item)
          label (if (> qty 1) (fmt/pluralize item) item)]
      (println qty label))))

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

