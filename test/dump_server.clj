(ns dump_server
  (:require [ring.adapter.jetty :as jetty]
            [test_utils :refer [read-file]]))

(defn handler
  ([{uri :uri}] (let [num (last (.split uri "/"))
                      fp (str "test/data/dmp" num)]
                  {:status 200
                   :body   (read-file fp)}))
  ([req respond raise] (respond (handler req))))

(defn start-server []
  (jetty/run-jetty handler {:port 3000
                            :join? false}))

(defn -main []
  (.join (start-server)))
