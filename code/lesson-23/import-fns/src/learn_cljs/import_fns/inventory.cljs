(ns learn-cljs.import-fns.inventory)

(defn make-inventory []
  {:items {}})

(defn- add-quantity [inventory-item qty]
  (update-in inventory-item [:qty]
    (fn [current-qty] (+ current-qty qty))))

(defn add-items
 ([inventory item] (add-items inventory item 1))
 ([inventory item qty]
  (update-in inventory [:items item]
    (fnil add-quantity
      {:item item :qty 0}
      qty))))

(defn list-items [inventory]
  (keys (:items inventory)))

(defn item-qty [inventory item]
  (get-in inventory [:items item :qty] 0))
