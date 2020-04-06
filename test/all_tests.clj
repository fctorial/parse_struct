(ns all-tests
  (:require [tester.core :refer :all]
            [deserializing_test]
            [serializing_test]
            [roundtrip_test]
            [clojure.pprint :refer [pprint]]))

(defn -main []
  (pprint (summarize-result (run-test (combine-tests [(serializing_test/make-test-suite)
                                                      (deserializing_test/make-test-suite)
                                                      (roundtrip_test/make-test-suite)])))))
