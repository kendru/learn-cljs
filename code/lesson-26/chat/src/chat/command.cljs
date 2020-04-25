(ns chat.command
  (:require [cljs.core.async :refer [go go-loop pub sub unsub chan <! put! close!]]))

(defonce interceptors (atom []))

(def cmd-ch (chan 1))
(def cmd-interceptor (chan 1))
(def cmd-bus (pub cmd-interceptor ::type))

(defn init! []
  (reset! interceptors [])
  (go-loop []
    (when-let [msg (reduce
                     (fn [msg [pred xform]]
                       (when (some? msg)
                         (if (pred msg)
                           (xform msg)
                           msg)))
                     (<! cmd-ch)
                     @interceptors)]
      (>! cmd-interceptor msg))
    (recur)))

(defn intercept! [pred xform]
  (swap! interceptors conj [pred xform]))

(defn dispatch!
 ([ch type] (dispatch! ch type nil))
 ([ch type payload]
  (put! ch {::type type
            ::payload payload})))

(defn handle! [p type handle]
  (let [sub-ch (chan)]
    (sub p type sub-ch)
    (go-loop []
      (handle (::payload (<! sub-ch)))
      (recur))))

(defn once! [p type handle]
  (let [sub-ch (chan)]
    (sub p type sub-ch)
    (go []
      (handle (::payload (<! sub-ch)))
      (unsub p type sub-ch)
      (close! sub-ch))))
