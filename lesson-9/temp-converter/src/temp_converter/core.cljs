(ns temp-converter.core
  (:require [goog.dom :as gdom]
            [goog.events :as gevents]))

(enable-console-print!)

(def celsius-radio (gdom/getElement "uom-c"))
(def fahrenheit-radio (gdom/getElement "uom-f"))
(def temp-input (gdom/getElement "temp"))
(def output-target (gdom/getElement "temp-out"))
(def output-unit-target (gdom/getElement "unit-out"))

(defn f->c [deg-f]
  (/ (- deg-f 32) 1.8))

(defn c->f [deg-c]
  (+ (* deg-c 1.8) 32))

(defn get-input-uom []
  (if (aget celsius-radio "checked")
    :celsius
    :fahrenheit))

(defn get-input-temp []
  (js/parseInt (aget temp-input "value")))

(defn set-output-temp [temp]
  (gdom/setTextContent output-target
                       (.toFixed temp 2)))

(defn update-output [_]
  (if (= :celsius (get-input-uom))
    (do (set-output-temp (c->f (get-input-temp)))
        (gdom/setTextContent output-unit-target "F"))
    (do (set-output-temp (f->c (get-input-temp)))
        (gdom/setTextContent output-unit-target "C"))))

(gevents/listen temp-input "keyup" update-output)
(gevents/listen celsius-radio "click" update-output)
(gevents/listen fahrenheit-radio "click" update-output)
