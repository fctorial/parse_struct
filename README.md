# parse_struct

Parse C struct dumps in clojure.

[![Clojars Project](https://img.shields.io/clojars/v/fctorial/parse_struct.svg)](https://clojars.org/fctorial/parse_struct)

## Installation:

#### Leiningen/Boot

    [fctorial/parse_struct "0.7.2"]

#### Clojure CLI/deps.edn

    fctorial/parse_struct {:mvn/version "0.7.2"}

#### Gradle

    compile 'fctorial:parse_struct:0.7.2'

#### Maven

```
<dependency>
  <groupId>fctorial</groupId>
  <artifactId>parse_struct</artifactId>
  <version>0.7.2</version>
</dependency>
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
; seq of unsigned integers parsed as big endian (long if they're too big, since java doesn't have unsigned. Large longs are stored in bigint)

(deserialize {:type        :struct
              :definition [[:a ct/u32]
                           [nil (ct/padding 2)]
                           [:b ct/i32]
                           [:c {:type       :string
                                :bytes      8
                                :trim_nulls true}]]}
             byte-seq)
; a struct

; ct/padding can be used in struct defs
; when serializing, padding bytes are returned as 0

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
* support cljs

### Tests:

You'll need to have `rustc` installed to run tests.

```
clojure -A:test-setup
clojure -A:test
```
