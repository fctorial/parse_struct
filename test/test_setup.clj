(ns test-setup
  (:require [popen :refer :all]
            [cljs_build]))

(defn -main []
  (exit-code (popen ["sh" "-c" "rm test/data/*"]))
  (let [rustc (popen ["rustc" "-C" "opt-level=0" "test/structs1.rs" "-o" "test/data/structs1"] :redirect true)]
    (if (not (zero? (exit-code rustc)))
      (do
        (println "rustc failed")
        (println (stdout rustc)))
      (if (not (zero? (exit-code (popen ["./test/data/structs1"]))))
        (println "dumper failed")
        (println "binary dumps: OK")))))
