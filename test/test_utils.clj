(ns test_utils
  (:import (java.nio.file Files Path)
           (java.net URI)
           (java.net.http HttpRequest HttpClient HttpResponse HttpResponse$BodyHandler HttpResponse$BodyHandlers)))

(def ^:private client (-> (HttpClient/newBuilder)
                          (.build)))

(defn read-dump [num]
  (let [uri (URI/create (str "http://localhost:3000/" num))
        req (-> (HttpRequest/newBuilder)
                (.GET)
                (.uri uri)
                (.build))
        resp (.send client req (HttpResponse$BodyHandlers/ofByteArray))]
    (.body resp)))

(defn read-file [fl]
  (Files/readAllBytes (Path/of fl (make-array String 0))))


