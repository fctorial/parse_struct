(ns deserializing_test
  (:require [tst.core :refer :all]
            [tst.predicates :refer :all]
            [parse_struct.core :refer [deserialize type-size]]
            [test_utils :refer [read-dump]]
            [dump_defs :refer :all]))

(defn make-test-suite []
  (combine-tests
    (for [i (range 1 11)]
      (testing :deserialization
               (testing (keyword (str "dump_" i))
                        (let [bs (read-dump i)
                              dump_def (deref (ns-resolve 'dump_defs (symbol (str "dump" i "_def"))))
                              dump_data (deref (ns-resolve 'dump_defs (symbol (str "dump" i "_data"))))
                              parsed (deserialize dump_def bs)]
                          (is (= dump_data parsed))))))))

