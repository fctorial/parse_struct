(ns all-tests
  (:require [tst.core :refer :all]
            [deserializing_test]
            [serializing_test]
            [lazy_serializing_test]
            [roundtrip_test]
            [clojure.pprint :refer [pprint]]))

(defn make-test-suite []
  (combine-tests [(serializing_test/make-test-suite)
                  (lazy_serializing_test/make-test-suite)
                  (deserializing_test/make-test-suite)
                  (roundtrip_test/make-test-suite)]))

(defn -main []
  (pprint (summarize-result (run-test (make-test-suite)))))
