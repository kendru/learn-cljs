(require '[cljs.build.api :as b])

(b/build "src"
  {:output-to "out/main.js"
   :output-dir "out"
   :main 'chat-backend.core
   :target :nodejs
   :optimizations :none
   :install-deps true
   :npm-deps {:pg "7.18.2"
              :ws "7.2.3"
              :db-migrate "0.11.6"
              :db-migrate-pg "1.0.0"}
  ;  :foreign-libs [{:file "src"
  ;                  :module-type :commonjs}]
   :verbose true})
