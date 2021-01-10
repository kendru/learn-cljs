(ns notes.ui.header
  (:require [reagent.core :as r]
            [notes.command :refer [dispatch!]]
            [notes.state :refer [app]]
            [notes.ui.util :refer [link]]))

;; (defn search []
;;   (let [input (r/cursor app [:search-input])]
;;     (fn []
;;       [:div.search-box
;;        [:input {:type "text"
;;                 :value @input
;;                 :on-change #(dispatch! :search/update-input
;;                                        (.. % -target -value))}]
;;        [:button.search-button
;;         {:on-click #(dispatch! :search/submit-input @input)}]])))

(defn header []
  [:header.page-header
  ;;  [link "Home" :home]
  ;;  [link "New Note" :home]
  ;;  [search]
   ])
