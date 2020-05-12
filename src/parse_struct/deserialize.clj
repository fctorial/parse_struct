(ns parse_struct.deserialize
  (:require [parse_struct.utils :refer [split-n take-exactly pow pows2 bitCount type-size zip-colls]]
            [parse_struct.common_types :refer :all]
            [clojure.spec.alpha :as s])
  (:import (java.nio ByteBuffer ByteOrder)))

(defmulti int-parser (fn [spec _] spec))

(defmacro make-int-parser [_spec]
  (let [spec (var-get (resolve _spec))
        bc (spec :bytes)
        signed? (spec :signed)
        bits (* bc 8)
        getter ({8  '.get
                 16 '.getShort
                 32 '.getInt
                 64 '.getLong} bits)
        endianness ({:little 'ByteOrder/LITTLE_ENDIAN
                     :big    'ByteOrder/BIG_ENDIAN} (spec :endianness))
        offset (pow 2 bits)
        data-arg (gensym "data")
        bb-var (gensym "bb")
        num-var (gensym "num")
        sign-handler-exp (if signed?
                           num-var
                           `(if (neg? ~num-var)
                              (+ ~num-var ~offset)
                              ~num-var))
        mname (symbol "int-parser")
        us (symbol "_")]
    `(defmethod ~mname ~_spec [~us ~data-arg]
       (let [~bb-var (.order (ByteBuffer/wrap (byte-array (take-exactly ~bc ~data-arg))) ~endianness)
             ~num-var (~getter ~bb-var)]
         ~sign-handler-exp))))

(make-int-parser i8)
(make-int-parser i16)
(make-int-parser i32)
(make-int-parser i64)
(make-int-parser u8)
(make-int-parser u16)
(make-int-parser u32)
(make-int-parser u64)

(make-int-parser i8be)
(make-int-parser i16be)
(make-int-parser i32be)
(make-int-parser i64be)
(make-int-parser u8be)
(make-int-parser u16be)
(make-int-parser u32be)
(make-int-parser u64be)

(defmulti _deserialize (fn [spec _] (spec :type)))

(defn deserialize [{adapter :adapter :as spec} data]
  ((or adapter
       identity) (_deserialize spec data)))

(defmethod _deserialize :int
  [spec data]
  (int-parser (dissoc spec :adapter) data))

(defmulti float-parser (fn [spec _] spec))

(defmacro make-float-parser [_spec]
  (let [spec (var-get (resolve _spec))
        {bc :bytes endianness :endianness} spec
        getter ({4 '.getFloat
                 8 '.getDouble} bc)
        endianness ({:little 'ByteOrder/LITTLE_ENDIAN
                     :big    'ByteOrder/BIG_ENDIAN} endianness)
        data-arg (gensym "data")
        bb-var (gensym "bb")
        res-var (gensym "num")
        mname (symbol "float-parser")
        us (symbol "_")]
    `(defmethod ~mname ~_spec [~us ~data-arg]
       (let [~bb-var (.order (ByteBuffer/wrap (byte-array (take-exactly ~bc ~data-arg))) ~endianness)
             ~res-var (~getter ~bb-var)]
         ~res-var))))

(make-float-parser f32)
(make-float-parser f32be)
(make-float-parser f64)
(make-float-parser f64be)

(defmethod _deserialize :float
  [spec data]
  (float-parser (dissoc spec :adapter) data))

(defmethod _deserialize :string
  [{bc :bytes} data]
  (new String (byte-array (take-exactly bc data)) "ASCII"))

(defmethod _deserialize :array
  [{ed :element n :len} data]
  (map
    (partial deserialize ed)
    (take-exactly n (let [sz (type-size ed)]
                      (if (zero? sz)
                        (repeat n [])
                        (partition sz data))))))

(defmethod _deserialize :struct
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

(defmethod _deserialize :padding
  [_ _])
