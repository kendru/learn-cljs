(ns chat-backend.util)

(defn is-error-clause? [expr]
  (and (sequential? expr)
       (= 'on-error (first expr))))

(defn parse-clauses [exprs]
  (let [[body error-clauses] (partition-by is-error-clause? exprs)
        [_ error-name & error-body] (first error-clauses)]
    {:body body
     :error-name error-name
     :error-body error-body}))

(defmacro <all
  "Given a set of bindings where the righthand evaluates to a channel that
  may succeed with `[:ok val]` or fail with `[:error err]`, run the first body
  clause with all bindings bound to the success values. A second clause may be
  given with a var and a body, in which case any error value will be handled by
  this clause, and the error value will be bound to the var given.

  Must be called from inside a go block.

  Example:

  (go
    (<all [v1 chan1
           v2 (get-chan)
           v3 chan3]
      (println v1)
      (println v2)
      (println v3)
      (on-error err
        (println \"Error:\" err))))"
  [bindings & exprs]
  (let [{:keys [body error-name error-body]} (parse-clauses exprs)
        val-sym (gensym)]
    (if (seq bindings)
      (let [[name expr & next] bindings]
        `(let [[status# ~val-sym] (cljs.core.async/<! ~expr)]
           (if (= status# :ok)
             (let [~name ~val-sym]
               (<all ~next ~@exprs))
             ~(if error-name
                `(let [~error-name ~val-sym] (do ~@error-body))
                'nil))))
      `(do ~@body))))
