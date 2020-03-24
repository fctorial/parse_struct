# parse_struct

Parse C struct dumps in clojure.

## Installation:

```
fctorial/parse_struct   {:mvn/version "0.7.1"}
```

## Usage:

```clojure
(ns examples
  (:require [parse_struct.core :refer [serialize deserialize type-size]]
            [parse_struct.common-types :as ct]))

(declare byte-seq)

(deserialize ct/i16 byte-seq)
; short integer

(deserialize {:type    :array
              :len     20
              :element ct/u32be}
             byte-seq)
; seq of big endian unsigned integers (long if they're too big, since java doesn't have unsigned. Large longs are stored in bigint)

(deserialize {:type        :struct
              :definition [[:a ct/i32]
                           [:b {:type       :string
                                :bytes      8
                                :trim_nulls true}]]}
             byte-seq)
; a struct

; For strings:
; :trim_nulls = true returns characters upto first null character,
; :trim_nulls = false keeps them in string

(serialize ct/u64 123456)
; serialize takes a spec of the same format, data which must conform to that spec (otherwise IllegalArgumentException)
; and returns a seq of bytes

(type-size {:type    :array
            :len     12
            :element {:type       :struct
                      :definition [[:a  ct/i32]
                                   [:b  ct/i16]]}})
; type-size gives the byte count of a spec
```

Structs and arrays can be arbitrarily nested.

### TODO:

* More tests for big endian
* spec
* support cljs

### Tests:

You'll need to have `rustc` installed to run tests.

```
clojure -A:test-setup
clojure -A:test
```
