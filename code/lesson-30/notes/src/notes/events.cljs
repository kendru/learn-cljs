(ns notes.events)

(def listeners (atom []))

(defn emit!
  ([type] (emit! type nil))
  ([type payload]
   (doseq [listen-fn @listeners]
     (listen-fn type payload))))

(defn register-listener! [listen-fn]
  (swap! listeners conj listen-fn))


(comment
  (register-listener!
   (fn [type payload]
     (println "Listener 1")
     (println "Type:" type "Payload:" payload)))

  (register-listener!
   (fn [type payload]
     (println "Listener 2")
     (println "Type:" type "Payload:" payload)))

  (emit! :foo {:name "cooper"})

  #_END_)
