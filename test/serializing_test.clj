(ns serializing_test
  (:require [clojure.test :refer :all]
            [dump_defs :refer :all]
            [parse_struct.serialize :refer [serialize]]
            [parse_struct.utils :refer [zip-colls]]
            [test_utils :refer [read-file]]
            [pjstadig.humane-test-output :as hto]))

(deftest serialization
  (hto/activate!)
  (doseq [i (range 1 10)
          :let [dump_file (str "test/data/dmp" i)
                dump_def (deref (ns-resolve 'dump_defs (symbol (str "dump" i "_def"))))
                dump_data (deref (ns-resolve 'dump_defs (symbol (str "dump" i "_data"))))]]
    (testing dump_file
      (let [bs (seq (read-file dump_file))
            generated (serialize dump_def dump_data)]
        (is (= bs generated))))))