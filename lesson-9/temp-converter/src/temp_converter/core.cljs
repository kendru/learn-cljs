(ns temp-converter.core
  (:require [goog.dom :as gdom]
            [goog.events :as gevents]))

(enable-console-print!)

(defn f->c [deg-f]
  (/ (- deg-f 32) 1.8))

(defn c->f [deg-c]
  (+ (* deg-c 1.8) 32))

(def celcius-radio (gdom/getElement "uom-c"))
(def farenheit-radio (gdom/getElement "uom-f"))
(def temp-input (gdom/getElement "temp"))
(def output-target (gdom/getElement "temp-out"))
(def output-unit-target (gdom/getElement "unit-out"))

(defn get-input-uom []
  (if (aget celcius-radio "checked")
    :celcius
    :farenheit))

(defn get-input-temp []
  (js/parseInt (aget temp-input "value")))

(defn set-output-temp [temp]
  (gdom/setTextContent output-target
                       (.toFixed temp 2)))

(defn update-output [_]
  (if (= :celcius (get-input-uom))
    (do (set-output-temp (c->f (get-input-temp)))
        (gdom/setTextContent output-unit-target "F"))
    (do (set-output-temp (f->c (get-input-temp)))
        (gdom/setTextContent output-unit-target "C"))))

(gevents/listen temp-input "keyup" update-output)
(gevents/listen celcius-radio "click" update-output)
(gevents/listen farenheit-radio "click" update-output)
