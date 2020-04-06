(ns tester.core
  (:require [tester.predicates :refer :all]))

(defmacro testing [name & body]
  (let [{subs :testing fns :main} (group-by #(if (and (seqable? %)
                                                      (= 'testing (first %)))
                                               :testing :main) body)
        res (into {} (map #(macroexpand %)
                          subs))]
    {name (if fns
            (assoc res :main `(with-meta (fn []
                                          ~@fns)
                                        {:code (quote ~fns)}))
            res)}))

(defn test-suite [& ts]
  (apply merge ts))

(defn run-test [ts]
  (into {} (for [[name body] ts]
             [name
              (if (= name :main)
                (try
                  (body)
                  (catch #?(:clj  Throwable
                            :cljs js/Object) e
                    {:result :EXCEPTION
                     :data   e}))
                (run-test body))])))

(defn -flatten-result [res]
  (reduce concat (for [[name body] res]
     (if (= :main name)
       [['(:main) body]]
       (map #(update % 0 (fn [e] (cons name e))) (-flatten-result body))))))

(defn flatten-result [res]
  (into {} (-flatten-result res)))

(defn treefy-result [res]
  (let [chs (group-by #(first (first %)) res)]
    (reduce
      (fn [r [name body]]
        (assoc r name (if (= name :main)
                        (second (first body))
                        (treefy-result (map (fn [[path body]]
                                              [(rest path) body]) body)))))
      {}
      chs)))

(defn get-failed [res]
  (->> res
       flatten-result
       (filter #(not= (:result (second %))
                      :OK))
       treefy-result))
