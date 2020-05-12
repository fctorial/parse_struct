(ns test-setup
  (:require [popen :refer :all]))

(defn -main []
  (exit-code (popen ["sh" "-c" "rm test/data/*"]))
  (let [gcc1 (popen ["gcc" "test/structs1.c" "-o" "test/data/structs1"] :redirect true)
        gcc2 (popen ["gcc" "test/structs2.c" "-o" "test/data/structs2"] :redirect true)]
    (if (and (not (zero? (exit-code gcc1)))
             (not (zero? (exit-code gcc2))))
      (do
        (println "gcc failed")
        (println (stdout gcc1))
        (println (stdout gcc2)))
      (do
        (if (not (zero? (exit-code (popen ["./test/data/structs1"]))))
          (println "dumper 1 failed")
          (println "dumper 1 OK"))
        (if (not (zero? (exit-code (popen ["./test/data/structs2"]))))
          (println "dumper 2 failed")
          (println "dumper 2 OK"))))))
