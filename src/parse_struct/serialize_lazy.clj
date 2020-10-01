(ns parse_struct.serialize_lazy
  (:require [parse_struct.utils :refer [split-n take-exactly pows2 bitCount pow in-range zip-colls type-size]]
            [parse_struct.common_types :refer [i8 i16 i32 i64 u8 u16 u32 u64 i8be i16be i32be i64be u8be u16be u32be u64be f32 f32be f64 f64be]])
  (:import (io.netty.buffer Unpooled ByteBuf)
           (java.nio.charset Charset)))

(defmulti serialize-lazy (fn [spec _] (spec :type)))

(defmethod serialize-lazy :array
  [{el :element len :len} v]
  (apply concat (map
                  #(serialize-lazy el %1)
                  (take-exactly len v))))

(defmethod serialize-lazy :struct
  [{items :definition} vm]
  (apply concat (for [[name spec] items
                      :let [v (vm name)]]
                  (if (and (nil? v)
                           (not= (spec :type) :padding))
                    (throw (new IllegalArgumentException (str "key " name " isn't present in the provided struct value")))
                    (serialize-lazy spec v)))))

(def b0 (byte 0))
(defmethod serialize-lazy :padding
  [{bc :bytes} _]
  (repeat bc b0))

(def ^Charset ascii (Charset/forName "US-ASCII"))
(defmethod serialize-lazy :string
  [{bc :bytes} ^String value]
  (let [bs (.getBytes value ascii)
        bb (Unpooled/buffer bc)]
    (if (> (count bs) bc)
      (throw (new IllegalArgumentException (str "string: \"" value "\" is bigger than the allotted space (" bc " bytes)")))
      (do
        (.writeBytes bb bs)
        (.writeBytes bb (byte-array (- bc (count value)) (byte 0)))
        (.array bb)))))

(defmulti float-serializer (fn [spec _] spec))

(defmethod serialize-lazy :float
  [spec ^Float value]
  (float-serializer spec value))

(defmulti int-serializer (fn [spec _] spec))

(defmethod serialize-lazy :int
  [spec ^Float value]
  (int-serializer spec value))

(defmacro make-float-serializer [_spec]
  (let [spec (var-get (resolve _spec))
        {size :bytes endianness :endianness} spec
        putter (get-in {:big    {4 '.writeFloat
                                 8 '.writeDouble}
                        :little {4 '.writeFloatLE
                                 8 '.writeDoubleLE}} [endianness size])
        caster ({4 float
                 8 double} size)
        spec-arg (gensym 1)
        value-arg (gensym 2)
        bb-var (with-meta (gensym 3) {:tag ByteBuf})
        name (symbol "float-serializer")]
    `(defmethod ~name ~spec
       [~spec-arg ~value-arg]
       (let [~bb-var (Unpooled/buffer ~size)]
         (~putter ~bb-var (~caster ~value-arg))
         (.array ~bb-var)))))

(make-float-serializer f32)
(make-float-serializer f64)
(make-float-serializer f32be)
(make-float-serializer f64be)

(defmacro make-int-serializer [_spec]
  (let [spec (var-get (resolve _spec))
        {size :bytes endianness :endianness signed :signed} spec
        min-range (if signed
                    ({1 Byte/MIN_VALUE
                      2 Short/MIN_VALUE
                      4 Integer/MIN_VALUE
                      8 Long/MIN_VALUE} size)
                    0)
        signed-max-range ({1 Byte/MAX_VALUE
                           2 Short/MAX_VALUE
                           4 Integer/MAX_VALUE
                           8 Long/MAX_VALUE} size)
        max-range (if signed
                    signed-max-range
                    (dec (pow 2 (* 8 size))))
        putter (get-in {:big    {1 '.writeByte
                                 2 '.writeShort
                                 4 '.writeInt
                                 8 '.writeLong}
                        :little {1 '.writeByte
                                 2 '.writeShortLE
                                 4 '.writeIntLE
                                 8 '.writeLongLE}} [endianness size])
        caster ({1 byte
                 2 short
                 4 int
                 8 long} size)
        unsigned-offsetter (if signed
                             'identity
                             (let [value-var (gensym)
                                   offset (pow 2 (* 8 size))]
                               `(fn [~value-var]
                                  (if (> ~value-var ~signed-max-range)
                                    (- ~value-var ~offset)
                                    ~value-var))))
        spec-arg (gensym 1)
        value-arg (gensym 2)
        bb-var (gensym 3)
        name (symbol "int-serializer")]
    `(defmethod ~name ~spec
       [~spec-arg ~value-arg]
       (if (not (<= ~min-range ~value-arg ~max-range))
         (throw (new IllegalArgumentException (str "number: " ~value-arg " is out of bounds for type " ~_spec)))
         (let [~bb-var (Unpooled/buffer ~size)]
           (~putter ~bb-var (~caster (~unsigned-offsetter ~value-arg)))
           (.array ~bb-var))))))

(make-int-serializer i8)
(make-int-serializer i16)
(make-int-serializer i32)
(make-int-serializer i64)
(make-int-serializer u8)
(make-int-serializer u16)
(make-int-serializer u32)
(make-int-serializer u64)

(make-int-serializer i8be)
(make-int-serializer i16be)
(make-int-serializer i32be)
(make-int-serializer i64be)
(make-int-serializer u8be)
(make-int-serializer u16be)
(make-int-serializer u32be)
(make-int-serializer u64be)
