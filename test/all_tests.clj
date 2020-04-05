(ns all-tests
  (:require [clojure.test :refer :all]
            [pjstadig.humane-test-output :as hto]
            [deserializing_test]
            [serializing_test]
            [roundtrip_test]
            [dump_server :refer [start-server]]))

(defn -main []
  (hto/activate!)
  (let [server (start-server)]
    (try
      (run-tests 'deserializing_test
                 'serializing_test
                 'roundtrip_test)
      (finally
        (.stop server)))))
