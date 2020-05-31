(ns lazy_serializing_test
  (:require [tst.core :refer :all]
            [dump_defs :refer :all]
            [parse_struct.serialize_lazy :refer [serialize-lazy]]
            [parse_struct.utils :refer [zip-colls]]
            [test_utils :refer [read-dump]]))

(defn make-test-suite []
  (combine-tests
    (for [i (map inc (range 10))]
      (testing :serialization
               (testing (keyword (str "dump_" i)) [bs dump_def dump_data generated]
                        (reset! bs (seq (read-dump i)))
                        (reset! dump_def (deref (ns-resolve 'dump_defs (symbol (str "dump" i "_def")))))
                        (reset! dump_data (deref (ns-resolve 'dump_defs (symbol (str "dump" i "_data")))))
                        (reset! generated (seq (serialize-lazy @dump_def @dump_data)))
                        (if (= @bs @generated)
                          {:result :OK}
                          {:result    :ERR
                           :message   (str "serializing dump " i " gives invalid result")}))))))
