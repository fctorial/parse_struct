(ns struct-generator
  (:require [parse_struct.common-types :refer :all]
            [parse_struct.utils :refer [pow type-size]]))

(defn rand-range [s e]
  (+ (long (rand (- e s))) s))

(defn half [n]
  (/ n 2))

(defn neg [n]
  (* -1 n))

(defn i [bits]
  (let [up-limit (pow 2 (dec bits))
        down-limit (neg up-limit)]
    (fn []
      (rand-range down-limit
                  up-limit))))

(defn u [bits]
  (let [up-limit (pow 2 bits)]
    (fn []
      (rand-range 0
                  up-limit))))

(defn lu []
  (bigint (rand (pow 2 64))))
(defn l []
  (- (lu) Long/MAX_VALUE))

(defn f []
  (float (* (rand) Float/MAX_VALUE)))
(defn d []
  (* (rand) Double/MAX_VALUE))

(def max_char (inc (int (Character/MAX_VALUE))))
(defn gen-name [n]
  (for [_ (range n)]
    (char (inc (rand-int 127)))))

(defn uuid []
  (apply str (repeatedly 10 #(rand-nth "qwertyuiopasdghklzxcvbnm1234567890"))))

(defn pad-nulls [s n]
  (apply str (take n (concat s (repeat (char 0))))))

(def prim-generators {:int    {true  {1 (i 8)
                                      2 (i 16)
                                      4 (i 32)
                                      8 l}
                               false {1 (u 8)
                                      2 (u 16)
                                      4 (u 32)
                                      8 lu}}
                      :float  {4 f
                               8 d}
                      :string {true  #(apply str (gen-name (rand-int %)))
                               false #(pad-nulls (gen-name (rand-int %))
                                                 %)}})

(defn gen-struct-val [spec]
  (case (spec :type)
    :int ((get-in prim-generators [:int (spec :signed) (spec :bytes)]))
    :string ((get-in prim-generators [:string (spec :trim_nulls)]) (spec :bytes))
    :float ((get-in prim-generators [:float (spec :bytes)]))
    :array (for [_ (range (spec :len))]
             (gen-struct-val (spec :element)))
    :struct (into {}
                  (map (fn [[name value]] [name (gen-struct-val value)]) (filter (fn [[_ s]]
                                                                                   (not= (s :type) :padding))
                                                                                 (spec :definition))))))

(def num_prims [i8
                u8
                i16
                u16
                i32
                u32
                i64
                u64
                f32
                f64
                i8be
                u8be
                i16be
                u16be
                i32be
                u32be
                i64be
                u64be
                f32be
                f64be])

(declare gen-rand-spec)

(defn gen-rand-prim-spec [_]
  (if (zero? (rand-int 10))
    {:type       :string
     :bytes      (rand-int 20)
     :encoding   (["UTF-8" "UTF-16" "ASCII"] (rand-int 3))
     :trim_nulls (zero? (rand-int 2))}
    (rand-nth num_prims)))

(defn gen-rand-array-spec [{max-len :max-array-len :as characteristics}]
  {:type    :array
   :len     (rand-int max-len)
   :element (gen-rand-spec (update characteristics :max-depth dec))})

(defn gen-rand-struct-spec [{max-children :max-struct-children :as characteristics}]
  (let [count (rand-int max-children)
        next-characteristics (update characteristics :max-depth dec)]
    {:type       :struct
     :definition (map (fn [_]
                        [(uuid) (if (zero? (rand-int 5))
                                  {:type :padding
                                   :bytes (rand-int 10)}
                                  (gen-rand-spec next-characteristics))])
                      (range count))}))

(defn gen-rand-spec [{max-depth :max-depth :as characteristics}]
  (if (zero? max-depth)
    (gen-rand-prim-spec nil)
    ((rand-nth [gen-rand-prim-spec
                gen-rand-array-spec
                gen-rand-struct-spec]) characteristics)))

