(ns async.core
  (:require [goog.dom :as gdom]
            [goog.events :as gevent]
            [cljs.core.async :refer [go go-loop chan <! >! put! timeout dropping-buffer]]))

(enable-console-print!)

(def query-input (gdom/getElement "query-input"))
(def results-display (gdom/getElement "query-results"))

(def keydown-ch (chan))
(gevent/listen js/document "keydown"
  #(put! keydown-ch (.-key %)))

(def keyup-ch (chan))
(gevent/listen js/document "keyup"
  #(put! keyup-ch (.-key %)))

(def is-modifier? #{"Control" "Meta" "Alt" "Shift"})

(def chord-ch (chan))
(go-loop [modifiers []
          pressed nil]
  (when (and (seq modifiers) pressed)
    (>! chord-ch (conj modifiers pressed)))
  (let [[key ch] (alts! [keydown-ch keyup-ch])]
    (condp = ch
      keydown-ch (if (is-modifier? key)
                   (recur (conj modifiers key) pressed)
                   (recur modifiers key))
      keyup-ch (if (is-modifier? key)
                 (recur (filterv #(not= % key) modifiers) pressed)
                 (recur modifiers nil)))))

(defn perform-request [in]
  (let [ch (chan)]
    (js/setTimeout
      #(put! ch (str "Results for: " in))
      (* 2000 (js/Math.random)))
    ch))

(go-loop []
  (let [chord (<! chord-ch)]
    (when (and (= chord ["Control" "r"])
               (= js/document.activeElement query-input))
      (aset results-display "innerText" "Loading...")
      (aset results-display "innerText"
        (<! (perform-request (.-value query-input)))))
    (recur)))
