(ns benchmark
  (:require [parse_struct.core :refer [serialize deserialize]]
            [parse_struct.common-types :refer :all]
            [struct-generator :refer [gen-struct-val gen-rand-spec]]
            [criterium.core :as cr]))

(defn -main []
  (let [spec (gen-rand-spec {:max-depth           2
                             :max-array-len       5
                             :max-struct-children 5})
        value (gen-struct-val spec)]
    (cr/with-progress-reporting (cr/quick-bench (= (deserialize spec (serialize spec value))
                                                   (seq value))
                                                :verbose))))