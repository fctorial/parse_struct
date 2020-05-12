(ns parse_struct.serialize
  (:require [parse_struct.utils :refer [split-n take-exactly pows2 bitCount pow in-range zip-colls type-size ascii]]
            [parse_struct.common_types :refer :all])
  (:import (io.netty.buffer Unpooled ByteBuf)))

(defmulti int-writer (fn [spec _ _] spec))

(defmacro make-int-writer [_spec]
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
        bb-arg (gensym 3)
        name (symbol "int-writer")]
    `(defmethod ~name ~spec
       [~spec-arg ~value-arg ~(with-meta bb-arg {:tag 'ByteBuf})]
       (if (not (<= ~min-range ~value-arg ~max-range))
         (throw (new IllegalArgumentException (str "number: " ~value-arg " is out of bounds for type " ~_spec)))
         (do
           (~putter ~bb-arg (~caster (~unsigned-offsetter ~value-arg))))))))

(make-int-writer i8)
(make-int-writer i16)
(make-int-writer i32)
(make-int-writer i64)
(make-int-writer u8)
(make-int-writer u16)
(make-int-writer u32)
(make-int-writer u64)

(make-int-writer i8be)
(make-int-writer i16be)
(make-int-writer i32be)
(make-int-writer i64be)
(make-int-writer u8be)
(make-int-writer u16be)
(make-int-writer u32be)
(make-int-writer u64be)

(defmulti float-writer (fn [spec _ bb] spec))

(defmacro make-float-writer [_spec]
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
        bb-arg (with-meta (gensym 3) {:tag ByteBuf})
        name (symbol "float-writer")]
    `(defmethod ~name ~spec
       [~spec-arg ~value-arg ~(with-meta bb-arg {:tag 'ByteBuf})]
       (~putter ~bb-arg (~caster ~value-arg)))))

(make-float-writer f32)
(make-float-writer f32be)
(make-float-writer f64)
(make-float-writer f64be)

(defmulti _serialize (fn [spec _ _] (spec :type)))

(defmethod _serialize :int
  [spec value bb]
  (int-writer (dissoc spec :adapter) value bb))

(defmethod _serialize :float [spec value bb]
  (float-writer (dissoc spec :adapter) value bb))

(defmethod _serialize :string
  [{bc :bytes} ^String value ^ByteBuf bb]
  (let [bs (.getBytes value ascii)]
    (if (> (count bs) bc)
      (throw (new IllegalArgumentException (str "string: \"" value "\" is bigger than the allotted space (" bc " bytes)")))
      (do
        (.writeBytes bb bs)
        (.writeBytes bb (byte-array (- bc (count value)) (byte 0)))))))

(defmethod _serialize :struct
  [{items :definition} value bb]
  (if (not= (set (map first
                      (filter (fn [[_ spec]]
                                (not= :padding (spec :type)))
                              items)))
            (set (keys value)))
    (throw (new IllegalArgumentException))
    (doseq [[name spec] items]
      (_serialize spec (value name) bb))))

(defmethod _serialize :array
  [{len :len element :element} value bb]
  (doseq [e (take-exactly len value)]
    (_serialize element e bb)))

(defmethod _serialize :padding
  [{bc :bytes} _ ^ByteBuf bb]
  (.writeBytes bb (byte-array bc)))

(defn serialize [spec value]
  (let [bb (Unpooled/buffer (type-size spec))]
    (_serialize spec value bb)
    (.array bb)))
