(ns roundtrip_test
  (:require [tst.core :refer :all]
            [clojure.data :refer [diff]]
            [parse_struct.deserialize :refer [deserialize]]
            [parse_struct.serialize :refer [serialize]]
            [parse_struct.serialize_lazy :refer [serialize-lazy]]
            [struct-generator :refer [gen-rand-spec gen-struct-val]]))

(defn unit-work [id]
  (testing :roundtrip
   (testing (keyword (str id)) [spec value after d]
            (reset! spec (gen-rand-spec {:max-array-len       (inc (rand-int 10))
                                         :max-struct-children (inc (rand-int 10))
                                         :max-depth           (inc (rand-int 3))}))
            (reset! value (gen-struct-val @spec))
            (reset! after (deserialize @spec ((if (even? id)
                                                serialize serialize-lazy) @spec @value)))
            (reset! d (diff @value @after))
            (if (and (nil? (first @d))
                     (nil? (second @d)))
              {:result :OK}
              {:result  :ERR
               :message "roundtrip test failed"}))))

(defn make-test-suite []
  (combine-tests (for [id (range 50000)]
                   (unit-work id))))
