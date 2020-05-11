(ns benchmark
  (:require [parse_struct.core :refer [serialize deserialize]]
            [parse_struct.common_types :refer :all]
            [struct-generator :refer [gen-struct-val gen-rand-spec]]
            [criterium.core :as cr]))

(defn -main []
  (let [spec (read-string (slurp "test/small_spec.edn"))
        value (gen-struct-val spec)]
    (cr/with-progress-reporting (cr/quick-bench (= (deserialize spec (serialize spec value))
                                                   (if (seqable? value)
                                                     (seq value)
                                                     value))
                                                :verbose))))