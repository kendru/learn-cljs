(ns chat.components.dom
  (:require [goog.dom :as gdom])
  (:import [goog.dom TagName]))

(defn dom-fn [tag-name]
  (fn [& args]
    (apply gdom/createDom tag-name args)))

(def a (dom-fn TagName.A))
(def article (dom-fn TagName.ARTICLE))
(def aside (dom-fn TagName.ASIDE))
(def button (dom-fn TagName.BUTTON))
(def div (dom-fn TagName.DIV))
(def form (dom-fn TagName.FORM))
(def header (dom-fn TagName.HEADER))
(def h1 (dom-fn TagName.H1))
(def i (dom-fn TagName.I))
(def input (dom-fn TagName.INPUT))
(def label (dom-fn TagName.LABEL))
(def p (dom-fn TagName.P))
(def section (dom-fn TagName.SECTION))
(def textarea (dom-fn TagName.TEXTAREA))

(defn with-children [el & children]
  (doseq [child children]
    (.appendChild el child))
  el)
