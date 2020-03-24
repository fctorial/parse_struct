(ns parse_struct.common-types)

(def i8 {:type   :int
         :bytes  1
         :signed true
         :endianness :little})

(def u8 {:type   :int
         :bytes  1
         :signed false
         :endianness :little})

(def i16 {:type   :int
          :bytes  2
          :signed true
          :endianness :little})

(def u16 {:type   :int
          :bytes  2
          :signed false
          :endianness :little})

(def i32 {:type   :int
          :bytes  4
          :signed true
          :endianness :little})

(def u32 {:type   :int
          :bytes  4
          :signed false
          :endianness :little})

(def i64 {:type   :int
          :bytes  8
          :signed true
          :endianness :little})

(def u64 {:type   :int
          :bytes  8
          :signed false
          :endianness :little})

(def f32 {:type :float
          :bytes 4
          :endianness :little})

(def i8be {:type   :int
           :bytes  1
           :signed true
           :endianness :big})

(def u8be {:type   :int
           :bytes  1
           :signed false
           :endianness :big})

(def i16be {:type   :int
            :bytes  2
            :signed true
            :endianness :big})

(def u16be {:type   :int
            :bytes  2
            :signed false
            :endianness :big})

(def i32be {:type   :int
            :bytes  4
            :signed true
            :endianness :big})

(def u32be {:type   :int
            :bytes  4
            :signed false
            :endianness :big})

(def i64be {:type   :int
            :bytes  8
            :signed true
            :endianness :big})

(def u64be {:type   :int
            :bytes  8
            :signed false
            :endianness :big})

(def f32be {:type :float
            :bytes 4
            :endianness :big})

(def f64 {:type :float
          :bytes 8
          :endianness :little})

(def name8 {:type       :string
            :bytes      8
            :trim_nulls true})

