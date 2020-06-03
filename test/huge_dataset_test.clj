(ns huge_dataset_test
  (:require [clojure.core.async :refer [go >! >!! <! <!! chan go-loop] :as async]
            [parse_struct.common_types :refer :all]
            [parse_struct.core :refer :all]
            [parse_struct.utils :refer [zip-colls]])
  (:import (java.util Random)))

(defn -< [ip ops]
  (go-loop []
    (let [val (<! ip)]
      (if val
        (do
          (doseq [op ops]
            (>! op val))
          (recur))
        (doseq [op ops]
          (async/close! op))))))

(defn chan->seq [ch]
  (when-let [val (<!! ch)]
    (lazy-seq (cons val (chan->seq ch)))))

(defn rand-chan [kb]
  (let [bgen (chan 10240)
        rnd (new Random)]
    (go
      (let [a (byte-array 1024)]
        (doseq [_ (range kb)]
          (.nextBytes rnd a)
          (doseq [b a]
            (>! bgen b)))
        (async/close! bgen)))
    bgen))

(defn gen-spec [kb]
  {:type    :array
   :len     kb
   :element {:type       :struct
             :definition [[:a {:type    :array
                               :len     64
                               :element u32}]
                          [:b {:type    :array
                               :len     64
                               :element i64}]
                          [:c {:type    :array
                               :len     64
                               :element f32be}]]}})

(defmacro df [ks vex]
  (let [vss (mapv gensym ks)
        ex (map
             (fn [[k vs]] (list 'def k vs))
             (zip-colls ks vss))]
    `(let [val# ~vex
           ~vss val#]
      ~@ex
      nil)))

(defn memory []
  (/ (.totalMemory (Runtime/getRuntime)) 1024 1024))
(def mmem (memory))
(defn -main []
  (let [mb 1024
        kb (* 1024 mb)
        bc (* 1024 kb)
        spec (gen-spec kb)
        bgen (rand-chan kb)
        sip (chan 10240)
        sop (chan 10240)
        tip (chan 10240)]
    (-< bgen [sip tip])
    (go
      (doseq [b (serialize-lazy spec (deserialize spec (chan->seq sip)))]
        (>! sop b))
      (async/close! sop))
    (<!! (go
           (doseq [[i b a] (zip-colls (range) (chan->seq tip) (chan->seq sop))]
             (when (zero? (mod i 5120))
               (let [percent (float (* 100 (/ i bc)))
                     mem (memory)]
                 (if (> mem mmem)
                   (def mmem mem))
                 (print "\033[2K")
                 (print (str (format "%.2f" percent) " percent done, PEAK HEAP SIZE: " mmem "mb"))))
             (assert (= b a)))
           (println :ALL_OK)))))
