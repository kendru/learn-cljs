(ns chat-backend.core
  (:require [cljs.nodejs :as nodejs]
            ["pg" :as pg]
            ["ws" :as ws]
            ["process" :as process]
            [cljs.core.async :refer [go go-loop pub sub unsub chan <! put! close!]]
            [chat-backend.user :as user]
            [chat-backend.service :as service]
            [chat-backend.util :refer [<fn <once <all]]))

(nodejs/enable-util-print!)

(defn get-env
  ([name fallback] (get-env name fallback identity))
  ([name fallback xform]
   (if-let [val (aget process/env name)]
     (xform val)
     fallback)))

(defn pg-pool []
  (pg/Pool. #js {"user" (get-env "POSTGRES_USER" "chat")
                 "password" (get-env "POSTGRES_PASSWORD" "s3cr3t")
                 "host" (get-env "POSTGRES_HOST" "127.0.0.1")
                 "database" (get-env "POSTGRES_DB" "chat")
                 "port" (get-env "POSTGRES_PORT" 5432 js/parseInt)}))

(defn ws-server []
  (let [port (get-env "HTTP_PORT" 8080 js/parseInt)]
    (ws/Server. #js {"port" port}
                #(println "Server listening on port:" port))))

(defn -main [& args]
  (let [pool (pg-pool)
        wss (ws-server)
        server-close-ch (<once wss "close")
        exit-cleanly (fn []
                       (.close wss)
                       (go (<all [_ (<fn #(.end pool %))
                                  _ server-close-ch])
                         (println "Exiting gracefully")
                         (.exit process 0)
                         (on-error e
                           (println "Error shutting down:" e)
                           (.exit process 1))))]
    (service/attach wss pool)
    (.on process "SIGINT" exit-cleanly)
    (.on process "SIGTERM" exit-cleanly)))


(set! *main-cli-fn* -main)
