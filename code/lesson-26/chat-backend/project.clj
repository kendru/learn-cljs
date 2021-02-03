(defproject chat-backend "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :min-lein-version "2.9.1"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [org.clojure/core.async  "0.4.500"]]

  :jvm-opts ^:replace ["-Xmx1g" "-server"])
