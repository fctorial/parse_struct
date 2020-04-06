(ns tester.predicates)

(defmacro is
  ([call msg]
   (let [e (gensym)]
     `(if ~call
        {:result :OK}
        {:result :ERR
         :data   (or ~msg
                     "predicate returned false")})))
  ([call] `(is ~call nil)))