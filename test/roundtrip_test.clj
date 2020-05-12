(ns roundtrip_test
  (:require [tst.core :refer :all]
            [clojure.data :refer [diff]]
            [parse_struct.deserialize :refer [deserialize]]
            [parse_struct.serialize :refer [serialize]]
            [struct-generator :refer [gen-rand-spec gen-struct-val]]))

(defn unit-work [id]
  (testing :roundtrip
   (testing (keyword (str id))
            (let [spec (gen-rand-spec {:max-array-len       (inc (rand-int 10))
                                       :max-struct-children (inc (rand-int 10))
                                       :max-depth           (inc (rand-int 3))})
                  value (gen-struct-val spec)
                  after (deserialize spec (serialize spec value))
                  d (diff value after)]
              (if (and (nil? (first d))
                       (nil? (second d)))
                {:result :OK}
                {:result :ERR
                 :message "roundtrip test failed"
                 :before value
                 :after after
                 :diff d
                 :spec spec})))))

(defn make-test-suite []
  (combine-tests (for [id (range 10000)]
                   (unit-work id))))
