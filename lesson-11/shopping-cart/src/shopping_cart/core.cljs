(ns shopping-cart.core
  (:require [goog.dom :as gdom]))

(def tax-rate 0.079)
(def cart [{:name "Silicone Pancake Mold" :price 10.49 :taxable? false}
           {:name "Small Pour-Over Coffee Maker" :price 18.96 :taxable? true}
           {:name "Digital Kitchen Scale" :price 24.95 :taxable? true}])

(defn add-sales-tax [cart-item]
  (assoc cart-item
         :sales-tax (* (:price cart-item) tax-rate)))

(def taxable-cart
  (map add-sales-tax
       (filter :taxable? cart)))

(def item-list (gdom/createDom "ul" nil ""))

;; Helper function to generate the display text for a product
(defn display-item [item]
  (str (:name item)
       ": "
       (:price item)
       " (tax: "
       (.toFixed (:sales-tax item) 2)
       ")"))

;; Create the list of products
(doseq [item taxable-cart]
  (gdom/appendChild
   item-list
   (gdom/createDom "li" #js {} (display-item item))))

;; Clear the entire document and append the list
(gdom/removeChildren js/document.body)
(gdom/appendChild js/document.body item-list)
