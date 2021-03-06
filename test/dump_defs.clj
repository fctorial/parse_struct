(ns dump_defs
  (:require [parse_struct.common_types :refer :all]
            [parse_struct.core :refer [trim-nulls-end]]))

(def name8 {:type    :string
            :bytes   8
            :adapter trim-nulls-end})

(def dump1_def {:type       :struct
                :definition [[:a i8]
                             [:b u8]
                             [:c i16],
                             [:d u16],
                             [:e i32],
                             [:f u32],
                             [:g name8],
                             [:h name8]]})

(def dump1_data {:a -100,
                 :b 200,
                 :c -32000,
                 :d 33000,
                 :e -2100000000,
                 :f 2200000000,
                 :g "name",
                 :h "namefull"})

(def dump2_def {:type    :array
                :len     20
                :element dump1_def})

(def dump2_data (repeat 20 dump1_data))

(def dump3_def {:type       :struct
                :definition [[:a i32]
                             [:c {:type  :string
                                  :bytes 6}]]})

(def dump3_data {:a 3000
                 :c "myname"})

(def dump4_def {:type    :array
                :len     10
                :element i32})

(def dump4_data (repeat 10 450))

(def dump5_def {:type    :array
                :len     20
                :element {:type    :array
                          :len     10
                          :element i32}})

(def dump5_data (repeat 20
                        (repeat 10 5)))

(def dump6_def {:type       :struct
                :definition [[:a i32]
                             [:b {:type       :struct
                                  :definition [[:a i32]
                                               [:c {:type  :string
                                                    :bytes 6}]]}]]})

(def dump6_data {:a -45
                 :b {:a 0
                     :c (str "here" (new String (byte-array [0 0])))}})

(def dump7_def {:type       :struct
                :definition [[:a u8]
                             [:b {:type    :array
                                  :len     3
                                  :element dump3_def}]]})

(def dump7_data {:a 200
                 :b (repeat 3
                            {:a -5
                             :c "anothe"})})

(def dump8_def {:type       :struct
                :definition {:a i64
                             :b {:type    :array
                                 :len     3
                                 :element u64}
                             :c f32
                             :d {:type    :array
                                 :len     5
                                 :element f64}}})

(def dump8_data {:a -6472394858488348972
                 :b (repeat 3 9823372036854775807)
                 :c 128.0
                 :d (repeat 5 256.0)})

(def dump9_def {:type       :struct
                :definition [[:a u16]
                             (padding 2)
                             [:b i32]]})

(def dump9_data {:a 40000
                 :b -40000})

(def dump10_def {:type    :array
                 :len     20
                 :element dump9_def})

(def dump10_data (repeat 20 dump9_data))

; only for deserialization and :adapter
(def dump11_def {:type    :array
                 :len     5
                 :element (assoc i16 :adapter (fn [x] (if (> x 0)
                                                        x (* -1 x))))})

(def dump11_data [12,
                  15,
                  4745,
                  434,
                  23455])

(def dump12_def {:type  :string
                 :bytes 15
                 :adapter #(Double/parseDouble %)})

(def dump12_data -23.55509)
