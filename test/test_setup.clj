(ns test-setup
  (:require [popen :refer :all]))

(defn -main []
  (exit-code (popen ["sh" "-c" "rm test/data/*"]))
  (let [gcc (popen ["gcc" "test/structs1.c" "-o" "test/data/structs1"] :redirect true)]
    (if (not (zero? (exit-code gcc)))
      (do
        (println "rustc failed")
        (println (stdout gcc)))
      (if (not (zero? (exit-code (popen ["./test/data/structs1"]))))
        (println "dumper failed")
        (println "OK")))))
