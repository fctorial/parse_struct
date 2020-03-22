(ns benchmark
  (:require [parse_struct.core :refer [serialize deserialize]]
            [parse_struct.common-types :refer :all]
            [roundtrip_test :refer [gen-struct-val]]))

(defn bench [f]
  (let [s (System/currentTimeMillis)]
    (time (f))
    (- (System/currentTimeMillis) s)))

(defn avg [coll]
  (float (/ (reduce + coll)
            (count coll))))

(def int-types [i8
                i16
                i32
                i64
                u8
                u16
                u32
                u64])

(defn reference-local [spec]
  (update-in spec [:element :definition]
             (fn [defs]
               (map
                 (fn [[n d]]
                   (let [sec (first (filter #(= d %) int-types))]
                     (if sec
                       [n sec]
                       [n d])))
                 defs))))

(defn -main []
  (let [spec (reference-local (read-string (slurp (str "test/big_spec.edn"))))
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
