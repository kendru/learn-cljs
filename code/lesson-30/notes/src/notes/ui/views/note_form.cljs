(ns notes.ui.views.note-form
  (:require [reagent.core :as r]
            [notes.state :refer [app]]
            [notes.ui.common :refer [button]]
            [notes.ui.tags :refer [tag-selector]]))

(defn input [data key label]
  (let [id (str "field-" (name key))]
    [:div.field
     [:div.label
      [:label {:for id} label]]
     [:div.control
      [:input {:id id
               :type "text"
               :on-change #(swap! data assoc key (.. % -target -value))
               :value (get @data key "")}]]]))

(defn textarea [data key label]
  (let [id (str "field-" (name key))]
    [:div.field
     [:div.label
      [:label {:for id} label]]
     [:div.control
      [:textarea {:id id
                  :on-change #(swap! data assoc key (.. % -target -value))
                  :value (get @data key "")}]]]))

(defn is-new? [data]
  (-> data :id nil?))

(defn submit-button [data]
  (let [[action text] (if (is-new? @data)
                        [:notes/create "Create"]
                        [:notes/update "Save"])]
    [button text {:dispatch [action @data]}]))

(defn note-form []
  (let [form-data (r/cursor app [:note-form])]
    (fn []
      [:div.note-form
       [:h2.page-title
        (if (is-new? @form-data) "New Note" "Edit Note")]
       [:section.editor
        [:form.note
         [input form-data :title "Title"]
         [textarea form-data :content "Content"]
         [submit-button form-data]]
        [:div.tags
         [:h3 "Tags"]
         (if (is-new? @form-data)
           [:p.help "Please save your note before adding tags."]
           [tag-selector])]]])))
