(ns parse_struct.serialize
  (:require [parse_struct.utils :refer [split-n take-exactly pows2 bitCount pow in-range zip-colls]]
            [parse_struct.common-types :refer :all])
  (:import (java.nio ByteBuffer ByteOrder)))

(defmulti int-writer (fn [spec _] spec))

(defmacro make-int-writer [_spec]
  (let [spec (var-get (resolve _spec))
        size (spec :bytes)
        min-range (if (spec :signed)
                    ({1 Byte/MIN_VALUE
                      2 Short/MIN_VALUE
                      4 Integer/MIN_VALUE
                      8 Long/MIN_VALUE} size)
                    0)
        signed-max-range ({1 Byte/MAX_VALUE
                           2 Short/MAX_VALUE
                           4 Integer/MAX_VALUE
                           8 Long/MAX_VALUE} size)
        max-range (if (spec :signed)
                    signed-max-range
                    (dec (pow 2 (* 8 size))))
        putter ({1 '.put
                 2 '.putShort
                 4 '.putInt
                 8 '.putLong} size)
        caster ({1 byte
                 2 short
                 4 int
                 8 long} size)
        endianness ({:little 'ByteOrder/LITTLE_ENDIAN
                     :big    'ByteOrder/BIG_ENDIAN} (spec :endianness))
        unsigned-offsetter (if (spec :signed)
                             'identity
                             (let [value-var (gensym)
                                   offset (pow 2 (* 8 size))]
                               `(fn [~value-var]
                                 (if (> ~value-var ~signed-max-range)
                                   (- ~value-var ~offset)
                                   ~value-var))))
        spec-arg (gensym)
        value-arg (gensym)
        bb-var (gensym)
        name (symbol "int-writer")]
    `(defmethod ~name ~spec
       [~spec-arg ~value-arg]
       (if (not (<= ~min-range ~value-arg ~max-range))
         (throw (new IllegalArgumentException (str "number: " ~value-arg " is out of bounds for type " ~_spec)))
         (let [~bb-var (ByteBuffer/allocate ~size)]
           (.order ~bb-var ~endianness)
           (~putter ~bb-var (~caster (~unsigned-offsetter ~value-arg)))
           (.array ~bb-var))))))

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

#_(defmacro make-int-writer [size]
  (let [min_range ({1 Byte/MIN_VALUE
                    2 Short/MIN_VALUE
                    4 Integer/MIN_VALUE
                    8 Long/MIN_VALUE} size)
        max_range ({1 Byte/MAX_VALUE
                    2 Short/MAX_VALUE
                    4 Integer/MAX_VALUE
                    8 Long/MAX_VALUE} size)
        putter ({1 '.put
                 2 '.putShort
                 4 '.putInt
                 8 '.putLong} size)
        caster ({1 byte
                 2 short
                 4 int
                 8 long} size)
        value-arg (gensym)
        bb-var (gensym)]
    `(fn [~value-arg]
       (if (not (<= ~min_range ~value-arg ~max_range))
         (throw (new IllegalArgumentException (str "number: " ~value-arg " is out of range")))
         (let [~bb-var (ByteBuffer/allocate ~size)]
           (.order ~bb-var ByteOrder/LITTLE_ENDIAN)
           (~putter ~bb-var (~caster ~value-arg))
           (.array ~bb-var))))))

#_(defmacro make-uint-writer [size]
  (let [putter ({1 '.put
                 2 '.putShort
                 4 '.putInt
                 8 '.putLong} size)
        caster ({1 byte
                 2 short
                 4 int
                 8 long} size)
        unsigned_off (pow 2 (* 8 size))
        unsigned_lim (/ unsigned_off 2)
        max_unsigned (dec unsigned_off)
        value-arg (gensym)
        bb-var (gensym)]
    `(fn [~value-arg]
       (if (not (<= 0 ~value-arg ~max_unsigned))
         (throw (new IllegalArgumentException (str "number: " ~value-arg " is out of range")))
         (let [~bb-var (ByteBuffer/allocate ~size)]
           (.order ~bb-var ByteOrder/LITTLE_ENDIAN)
           (~putter ~bb-var (~caster (if (>= ~value-arg ~unsigned_lim)
                                       (- ~value-arg ~unsigned_off)
                                       ~value-arg)))
           (.array ~bb-var))))))

(defmulti serialize (fn [spec _] (spec :type)))

(defmethod serialize :int
  [spec value]
  (int-writer spec value))

(defmacro make-float-writer [size]
  (let [putter ({4 '.putFloat
                 8 '.putDouble} size)
        val-arg (gensym)
        bb-var (gensym)]
   `(fn [~val-arg]
     (let [~bb-var (ByteBuffer/allocate ~size)]
       (.order ~bb-var ByteOrder/LITTLE_ENDIAN)
       (~putter ~bb-var ~val-arg)
       (.array ~bb-var)))))

(def float-writers {4 (make-float-writer 4)
                    8 (make-float-writer 8)})

(defmethod serialize :float [{bc :bytes} value]
  ((float-writers bc) value))

(defmethod serialize :string
  [{bc :bytes} value]
  (if (> (count value) bc)
    (throw (new IllegalArgumentException (str "string: \"" value "\" is longer than the allotted space (" bc " bytes)")))
    (if (not (every? #(<= 0 (int %) 127) value))
      (throw (new IllegalArgumentException (str "string: \"" value "\" is not ascii")))
      (concat (.getBytes value) (repeat (- bc (count value)) (byte 0))))))

(defmethod serialize :struct
  [{items :definition} value]
  (if (not (= (set (map first
                    (filter (fn [[_ spec]]
                              (not= :padding (spec :type)))
                            items)))
              (set (keys value))))
    (throw (new IllegalArgumentException value))
    (apply concat (for [[name spec] items]
                    (serialize spec (value name))))))

(defmethod serialize :array
  [{len :len element :element} value]
  (apply concat (map #(serialize element %) (take-exactly len value))))
