(ns all-tests
  (:require [tester.core :refer :all]
            [pjstadig.humane-test-output :as hto]
            [deserializing_test]
            [serializing_test]
            [roundtrip_test]
            [dump_server :refer [start-server]]))

(defn -main []
  (run-test (combine-tests [(serializing_test/make-test-suite)
                            (deserializing_test/make-test-suite)
                            (roundtrip_test/make-test-suite)])))
