(ns deserializing_test
  (:require [tst.core :refer :all]
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
                          (if (= parsed dump_data)
                            {:result :OK}
                            {:result :ERR
                             :message (str "deserializing dump " i " gives invalid result")
                             :dump_def dump_def
                             :expected dump_data
                             :actual parsed})))))))

