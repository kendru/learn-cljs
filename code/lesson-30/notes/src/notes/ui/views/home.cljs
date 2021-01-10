(ns notes.ui.views.home
  (:require [reagent.core :as r]
            [notes.state :refer [app]]
            [notes.api :as api]))


(defn tags-list []
  [:div.tags-list "TODO: Tags"])

;; The home view displays a list of notes on the left and a list of tags on the right.
(defn home []
  [:div.home
   [tags-list]])
