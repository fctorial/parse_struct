(ns serializing_test
  (:require [tester.core :refer :all]
            [tester.predicates :refer :all]
            [dump_defs :refer :all]
            [parse_struct.serialize :refer [serialize]]
            [parse_struct.utils :refer [zip-colls]]
            [test_utils :refer [read-dump]]))

(defn make-test-suite []
  (combine-tests
    (for [i (range 1 11)]
      (testing :serialization
               (testing (keyword (str "dump_" i))
                        (let [bs (seq (read-dump i))
                              dump_def (deref (ns-resolve 'dump_defs (symbol (str "dump" i "_def"))))
                              dump_data (deref (ns-resolve 'dump_defs (symbol (str "dump" i "_data"))))
                              generated (serialize dump_def dump_data)]
                          (is (= bs generated))))))))
