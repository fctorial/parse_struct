(ns deserializing_test
  (:require [tst.core :refer :all]
            [parse_struct.core :refer [deserialize type-size]]
            [test_utils :refer [read-dump]]
            [dump_defs :refer :all]))

(defn make-test-suite []
  (combine-tests
    (for [i (map inc (range 12))]
      (testing :deserialization
               (testing (keyword (str "dump_" i)) [bs dump_def dump_data parsed]
                        (reset! bs (seq (read-dump i)))
                        (reset! dump_def (deref (ns-resolve 'dump_defs (symbol (str "dump" i "_def")))))
                        (reset! dump_data (deref (ns-resolve 'dump_defs (symbol (str "dump" i "_data")))))
                        (reset! parsed (deserialize @dump_def @bs))
                        (if (= @parsed @dump_data)
                          {:result :OK}
                          {:result   :ERR
                           :message  (str "deserializing dump " i " gives invalid result")}))))))

