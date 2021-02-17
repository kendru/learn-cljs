(ns learn-cljs.errors
  (:require [learn-cljs.errors.exceptions :as exceptions]
            [learn-cljs.errors.conditions :as conditions]))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Uncomment one of the lines below to cause different errors to occur
;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;
;; Will fail parsing
; (.setItem js/localStorage "current-user" "{\"id:\"123,")

;;;;
;; Will fail validation of required attributes
; (.setItem js/localStorage "current-user" "{\"id\":123}")

;;;;
;; Will fail validation of allowed attributes
; (.setItem js/localStorage "current-user" "{\"id\":123,\"email\":\"test\",\"bogus\":1.6}")

; (exceptions/run)
(conditions/run)
