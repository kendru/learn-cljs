(ns learn-cljs.notes.ui.card
  (:require [reagent.core :as r]))

(defn card []
  (into [:div.card] (r/children (r/current-component))))

(defn text-header [title]
  [:div.card-header
    [:div.card-header-title
      [:p title]]])

(defn content []
  (into [:div.card-content] (r/children (r/current-component))))

(defn footer []
  (into [:div.card-footer] (r/children (r/current-component))))

(defn footer-item []
  (into [:div.card-footer-item] (r/children (r/current-component))))
