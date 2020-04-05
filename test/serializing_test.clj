(ns serializing_test
  (:require [clojure.test :refer :all]
            [dump_defs :refer :all]
            [parse_struct.serialize :refer [serialize]]
            [parse_struct.utils :refer [zip-colls]]
            [test_utils :refer [read-dump]]
            [pjstadig.humane-test-output :as hto]))

(deftest serialization
  (hto/activate!)
  (doseq [i (range 1 11)
          :let [bs (seq (read-dump i))
                dump_def (deref (ns-resolve 'dump_defs (symbol (str "dump" i "_def"))))
                dump_data (deref (ns-resolve 'dump_defs (symbol (str "dump" i "_data"))))]]
    (testing (str "dump: " i)
      (let [generated (serialize dump_def dump_data)]
        (is (= bs generated))))))