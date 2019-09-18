(ns passwords.core
  (:require [goog.dom :as gdom]
            [goog.events :as gevents]))

(enable-console-print!)

(defn values-same? [field-1 field-2]
  (= (aget field-1 "value")
     (aget field-2 "value")))

(defn handle-change [password confirmation status]
  (gdom/setTextContent status
                       (if (values-same? password confirmation)
                         "Matches"
                         "Do not match")))

(let [password (gdom/createElement "input")
      confirmation (gdom/createElement "input")
      status (gdom/createElement "p")
      app (gdom/getElement "app")]
  (gdom/setProperties password #js {"type" "password"})
  (gdom/setProperties confirmation #js {"type" "password"})

  (gevents/listen password goog.events/EventType.KEYUP
                  #(handle-change password confirmation status))
  (gevents/listen confirmation goog.events/EventType.KEYUP
                  #(handle-change password confirmation status))
  
  (gdom/setTextContent app "")
  (gdom/appendChild app password)
  (gdom/appendChild app confirmation)
  (gdom/appendChild app status))
