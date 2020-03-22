(ns benchmark
  (:require [parse_struct.core :refer [serialize deserialize]]
            [roundtrip_test :refer [gen-struct-val]]))

(defn bench [f]
  (let [s (System/currentTimeMillis)]
    (time (f))
    (- (System/currentTimeMillis) s)))

(defn avg [coll]
  (float (/ (reduce + coll)
            (count coll))))

(defn -main []
  (let [spec (read-string (slurp (str "test/big_spec.edn")))
        value (gen-struct-val spec)]
    (println "warming up")
    (doseq [i (range 1 10)]
      (println i)
      (count (deserialize spec (serialize spec value))))
    (println "done")
    (let [average (avg (for [_ (range 20)]
                         (bench #(= (deserialize spec (serialize spec value))
                                    (seq value)))))]
      (println "average time: " average)
      average)))
