(ns test-setup
  (:require [popen :refer :all])
  (:import (java.util.stream Collectors)))

(defn read-reader [rd]
  (.collect (.lines rd) (Collectors/joining "\n")))

(defn -main []
  (exit-code (popen ["sh" "-c" "rm test/data/*"]))
  (let [gcc1 (popen ["gcc" "test/structs1.c" "-o" "test/data/structs1"] :redirect true)
        gcc2 (popen ["gcc" "test/structs2.c" "-o" "test/data/structs2"] :redirect true)]
    (cond
      (not (zero? (exit-code gcc1))) (do
                                       (println "gcc1 failed")
                                       (println (read-reader (stdout gcc1))))
      (not (zero? (exit-code gcc2))) (do
                                       (println "gcc2 failed")
                                       (println (read-reader (stdout gcc2))))
      :else (do
              (if (not (zero? (exit-code (popen ["./test/data/structs1"]))))
                (println "dumper 1 failed")
                (println "dumper 1 OK"))
              (if (not (zero? (exit-code (popen ["./test/data/structs2"]))))
                (println "dumper 2 failed")
                (println "dumper 2 OK"))))))
