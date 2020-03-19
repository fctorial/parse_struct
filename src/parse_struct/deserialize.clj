(ns parse_struct.deserialize
  (:require [parse_struct.utils :refer [split-n take-exactly pow bitCount type-size]])
  (:import (java.nio ByteBuffer ByteOrder)))

(defn parseInt32 [bytes]
  (.getInt (.order (ByteBuffer/wrap (byte-array bytes)) ByteOrder/LITTLE_ENDIAN)))

(defn parseInt16 [bytes]
  (.getShort (.order (ByteBuffer/wrap (byte-array bytes)) ByteOrder/LITTLE_ENDIAN)))

(defn parseInt8 [bytes]
  (.get (ByteBuffer/wrap (byte-array bytes))))

(defmacro make-unsigned-maker [bits]
  (let [target-type (if (< bits 64)
                      long
                      bigint)
        numarg (gensym)]
    `(fn [~numarg]
      (if (< ~numarg 0)
        (+ (~target-type ~numarg) (pow 2 ~bits))
        ~numarg))))

(def intParsers {1 [parseInt8 (make-unsigned-maker 8)]
                 2 [parseInt16 (make-unsigned-maker 16)]
                 4 [parseInt32 (make-unsigned-maker 32)]})

(defn parse-int [{bc :bytes signed? :signed} data]
  (let [[parser sign-handler] (intParsers bc)
        parsedInt (parser (take-exactly bc data))
        signHandled (if (not signed?)
                      (sign-handler parsedInt)
                      parsedInt)]
    signHandled))

(defn parse-string [{bc :bytes trim_nulls? :trim_nulls} data]
  (let [chunk (take-exactly bc data)
        trimmed (if (not= trim_nulls? false)
                  (take-while #(not= 0 %) chunk)
                  chunk)]
    (new String (byte-array trimmed))))

(declare parse-struct)
(declare parse-array)
(declare deserialize)
(declare parsers)

(defn parse-array [{ed :element n :len} data]
  (map
    (partial deserialize ed)
    (take-exactly n (partition (type-size ed) data))))

(defn parse-struct [{definition :definition} data]
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

(defn deserialize [spec data]
  ((parsers (spec :type)) spec data))

(def parsers {:int    parse-int
              :string parse-string
              :array  parse-array
              :struct parse-struct
              :padding (fn [_ _])})