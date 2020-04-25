(ns chat-backend.util
  (:require-macros [chat-backend.util])
  (:require [cljs.core.async :refer [chan put! close!]]))

(defn <fn [f & args]
  (let [ch (chan 1)]
    (apply f (conj (vec args)
               (fn [err res]
                 (if err
                   (put! ch [:error err])
                   (put! ch [:ok res])))))
    ch))

(defn <on [obj msg]
  (let [ch (chan 1)]
    (.on obj msg
      (fn [& msg-args]
        (put! ch msg-args)))
    ch))

(defn <once [obj msg]
  (let [ch (chan 1)]
    (.once obj msg
      (fn [& msg-args]
        (put! ch msg-args)
        (close! ch)))
    ch))
