(ns notes.ui.sidebar
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]
            [notes.state :refer [app]]
            [notes.command :refer [dispatch!]]
            [notes.ui.util :refer [link]]))

(defn sorter [property dir]
  (fn [a b]
    (let [compare-fn (if (= :asc dir) < >)]
      (compare-fn (get a property)
                  (get b property)))))

(defn notes-list []
  (let [notes (r/cursor app [:data :notes])
        notes-list (ratom/make-reaction
                    #(let [{:keys [order-by order-dir by-id]} @notes]
                       (->> by-id
                            (vals)
                            (sort (sorter order-by order-dir))
                            (take 10))))]
    (dispatch! :notes/get-notes)
    (fn []
      [:ul.notes-list
       (for [note @notes-list]
         ^{:key (:id note)}
         [:li [link (:title note) {:route-params [:edit-note {:note-id (:id note)}]}]])])))

(defn sidebar []
  [:nav.sidebar
   [notes-list]])
