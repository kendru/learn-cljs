(ns notes.ui.tags
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]
            [notes.state :refer [app]]
            [notes.ui.common :refer [button]]
            [notes.command :refer [dispatch!]]))

(defn name-sorter [a b]
  (< (:name a) (:name b)))

(def all-tags
  (r/cursor app [:data :tags]))

(def tags-by-note-index
  (r/cursor app [:data :notes-tags :by-note-id]))

(def editing-note-id
  (r/cursor app [:note-form :id]))

(def note-tags
  (ratom/make-reaction
   #(get @tags-by-note-index @editing-note-id)))

(def attached-tags
  (ratom/make-reaction
   #(->> (select-keys @all-tags @note-tags)
         (vals)
         (sort name-sorter))))

(def available-tags
  (ratom/make-reaction
   #(->> (apply dissoc @all-tags @note-tags)
         (vals)
         (sort name-sorter))))

(defn attached-tag-list []
  [:div.attached
   (for [tag @attached-tags
         :let [{:keys [id name]} tag]]
     ^{:key id}
     [:span.tag name])])

(defn available-tags-list []
  [:div
   (for [tag @available-tags
         :let [{:keys [id name]} tag]]
     ^{:key id}
     [:div.tag {:on-click #(dispatch! :notes/tag {:note-id @editing-note-id
                                                  :tag-id id})}
      [:span.add "+"] name])])

(defn create-tag-input []
  (let [tag-name (r/atom "")]
    (fn []
      [:div.create-tag
       "Add: "
       [:input {:value @tag-name
                :on-key-up #(when (= (.-key %) "Enter")
                              (dispatch! :tags/create @tag-name)
                              (reset! tag-name ""))
                :on-change #(reset! tag-name (.. % -target -value))}]])))

(defn available-tag-selector []
  (let [is-expanded? (r/atom false)]
    (dispatch! :tags/get-tags)
    (fn []
      [:div.available
       (if @is-expanded?
         [:div.tag-selector
          [available-tags-list]
          [create-tag-input]
          [button "Close" {:class "block"
                           :on-click #(reset! is-expanded? false)}]]
         [button "+ Add Tags" {:class "block"
                               :on-click #(reset! is-expanded? true)}])])))

(defn tag-selector []
  [:div.tag-selector
   [attached-tag-list]
   [available-tag-selector]])
