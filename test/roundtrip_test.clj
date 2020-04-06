(ns roundtrip_test
  (:require [tester.core :refer :all]
            [tester.predicates :refer :all]
            [parse_struct.deserialize :refer [deserialize]]
            [parse_struct.serialize :refer [serialize]]
            [struct-generator :refer [gen-rand-spec gen-struct-val]]))

(defn unit-work [id]
  (testing :roundtrip
   (testing (keyword (str id))
            (let [spec (gen-rand-spec {:max-array-len       (inc (rand-int 10))
                                       :max-struct-children (inc (rand-int 10))
                                       :max-depth           (inc (rand-int 3))})
                  value (gen-struct-val spec)]
              (is (= value (deserialize spec (serialize spec value)))
                  (let [spec_file (str "test/data/failed_spec_" id ".edn")
                        value_file (str "test/data/failed_value_" id ".edn")]
                    (clojure.pprint/pprint spec (clojure.java.io/writer spec_file))
                    (clojure.pprint/pprint value (clojure.java.io/writer value_file))
                    (str "serialize-deserialize roundtrip failed for dump number: " id "\n"
                         "spec saved to: " spec_file "\n"
                         "value saved to: " value_file)))))))

(defn make-test-suite []
  (combine-tests (for [id (range 1000)]
                   (unit-work id))))
