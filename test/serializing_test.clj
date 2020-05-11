(ns serializing_test
  (:require [tst.core :refer :all]
            [tst.predicates :refer :all]
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
                              generated (seq (serialize dump_def dump_data))]
                          (if (= bs generated)
                            {:result :OK}
                            {:result :ERR
                             :message (str "serializing dump " i " gives invalid result")
                             :dump_def dump_def
                             :dump_data dump_data
                             :expected bs
                             :actual generated})))))))
