(ns learn-cljs.notes.ui.header
  (:require ;[reagent.core :as r]
            ;[learn-cljs.notes.command :refer [dispatch!]]
            ;[learn-cljs.notes.state :refer [app]]
            [learn-cljs.notes.ui.common :refer [button]]))

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
   [button "+ New Note" {:route-params [:create-note]
                         :class "inverse"}]])
