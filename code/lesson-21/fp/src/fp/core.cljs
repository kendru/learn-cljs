(ns fp.core
  (:require [clojure.string :as s]))

(enable-console-print!)

(def alan-p {:first-name "Alan"
             :last-name "Perlis"
             :online? false})

(def alan-t {:first-name "Alan"
             :last-name "Turing"
             :nickname "The Machine"
             :online? false})

(def robert-m {:first-name "Robert"
               :last-name "Morris"
               :online? true
               :hide? true})

(def users [alan-p alan-t robert-m])

(defn nickname [user]
  (or (:nickname user)
      (->> user
           ((juxt :first-name :last-name))
           (s/join " "))))

(defn bold [child]
  [:strong child])

(defn concat-strings [s1 s2]
  (s/trim (str s1 " " s2)))

(defn with-class [dom class-name]
  (if (map? (second dom))
    (update-in dom [1 :class] concat-strings class-name)
    (let [[tag & children] dom]
      (vec (concat [tag {:class class-name}]
                   children)))))

(defn with-status [dom entity]
  (with-class dom
    (if (:online? entity) "online" "offline")))

(defn user-status [user]
  [:div {:class "user-status"}
    ((juxt
      (comp bold nickname)
      (partial with-status [:span {:class "status-indicator"}]))
     user)])

(def test-a
  (into [] (map user-status users)))
