(ns parse_struct.deserialize
  (:require [parse_struct.utils :refer [split-n take-exactly pow pows2 bitCount type-size zip-colls]]
            [parse_struct.common-types :refer :all]
            [clojure.spec.alpha :as s])
  (:import (java.nio ByteBuffer ByteOrder)))

(defmacro make-int-parser [bits signed?]
  (let [bc (/ bits 8)
        getter ({8  '.get
                 16 '.getShort
                 32 '.getInt
                 64 '.getLong} bits)
        offset (pow 2 bits)
        data-arg (gensym)
        bb-var (gensym)
        num-var (gensym)
        sign-handler-exp (if signed?
                           num-var
                           `(if (neg? ~num-var)
                              (+ ~num-var ~offset)
                              ~num-var))]
    `(fn [~data-arg]
       (let [~bb-var (.order (ByteBuffer/wrap (byte-array (take-exactly ~bc ~data-arg))) ByteOrder/LITTLE_ENDIAN)
             ~num-var (~getter ~bb-var)]
         ~sign-handler-exp))))

(def int-parsers {i8  (make-int-parser 8 true)
                  i16 (make-int-parser 16 true)
                  i32 (make-int-parser 32 true)
                  i64 (make-int-parser 64 true)
                  u8  (make-int-parser 8 false)
                  u16 (make-int-parser 16 false)
                  u32 (make-int-parser 32 false)
                  u64 (make-int-parser 64 false)})

(defmulti deserialize (fn [spec _] (spec :type)))

(defmethod deserialize :float
  [{bc :bytes} data]
  (let [bb (.order (ByteBuffer/wrap (byte-array (take-exactly bc data))) ByteOrder/LITTLE_ENDIAN)]
    (case bc
      4 (.getFloat bb)
      8 (.getDouble bb)
      (throw (new IllegalArgumentException "Floats can have 4 or 8 bytes")))))

(defmethod deserialize :int
  [spec data]
  ((int-parsers spec) data))

(defmethod deserialize :string
  [{bc :bytes trim_nulls? :trim_nulls} data]
  (let [chunk (take-exactly bc data)
        trimmed (if (not= trim_nulls? false)
                  (take-while #(not= 0 %) chunk)
                  chunk)]
    (new String (byte-array trimmed) "ASCII")))

(defmethod deserialize :array
  [{ed :element n :len} data]
  (map
    (partial deserialize ed)
    (take-exactly n (let [sz (type-size ed)]
                      (if (zero? sz)
                        (repeat n [])
                        (partition sz data))))))

(defmethod deserialize :struct
  [{definition :definition} data]
  (loop [res {}
         items_left definition
         data_left data]
    (if (empty? items_left)
      res
      (let [[name spec] (first items_left)
            size (type-size spec)
            [curr_chunk next_data_left] (split-n size data_left)
            val (deserialize spec curr_chunk)]
        (recur (if (= (spec :type) :padding)
                 res
                 (assoc res name val))
               (rest items_left)
               next_data_left)))))

(defmethod deserialize :padding
  [_ _])
