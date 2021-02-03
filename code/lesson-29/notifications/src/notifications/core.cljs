(ns notifications.core
  (:require [reagent.dom :as rdom]
            [goog.dom :as gdom]
            [notifications.pubsub :as pubsub]
            [notifications.command-event :as command-event]
            [notifications.actor :as actor]))

(rdom/render
  ; [pubsub/app]
  ; [command-event/app]
  [actor/app]
  (gdom/getElement "app"))

(enable-console-print!)

