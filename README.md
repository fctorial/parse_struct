# parse_struct

Serialize/Deserialize binary data in clojure with an api similar
to pointer casting in `C`.

[![Clojars Project](https://img.shields.io/clojars/v/fctorial/parse_struct.svg)](https://clojars.org/fctorial/parse_struct)

## Installation:

#### Leiningen/Boot

    [fctorial/parse_struct "0.8.0"]

#### Clojure CLI/deps.edn

    fctorial/parse_struct {:mvn/version "0.8.0"}

#### Gradle

    compile 'fctorial:parse_struct:0.8.0'

#### Maven

```xml
<dependency>
  <groupId>fctorial</groupId>
  <artifactId>parse_struct</artifactId>
  <version>0.8.0</version>
</dependency>
```

## Examples

```clojure
(ns examples
  (:require [parse_struct.core :refer [serialize deserialize type-size]]
            [parse_struct.common_types :as ct]))

(declare byte-seq)

(deserialize ct/i16 byte-seq)
; short integer

(deserialize {:type    :array
              :len     20
              :element ct/u32be}
             byte-seq)
; seq of big endian unsigned integers (long if they're too big, since java doesn't have unsigned. Large longs are stored in bigint)

(deserialize {:type        :struct
              :definition [[:a ct/u32]
                           (ct/padding 2)
                           [:b ct/i32]
                           [:c {:type       :string
                                :bytes      8}]]}
             byte-seq)
; a struct
```

See wiki for full documentation.

### TODO:

* support cljs
* eager deserializer and tests
* abstracted tests

### Tests:

You'll need to have `gcc` installed to run tests.

```sh
clojure -A:test-setup
clojure -A:dump-server
# in other window
clojure -A:test
```
