(ns virtualkeiko.web
  (:use ring.adapter.jetty)
  (:use virtualkeiko.handler))

(defn -main []
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (initalize-app)
    (run-jetty app {:port port})))
