(ns tester.predicates)

(defn eq
  ([val1 val2] (eq val1 val2 nil))
  ([val1 val2 msg] (if (= val1 val2)
                     {:result :OK}
                     {:result  :ERR
                      :message (or msg
                                   "equality test failed")
                      :val1    val1
                      :val2    val2})))

(defn is
  ([pred & msg] (if pred
                  {:result :OK}
                  {:result :ERR
                   :message (or msg "predicate evaluated to false")
                   :value pred})))