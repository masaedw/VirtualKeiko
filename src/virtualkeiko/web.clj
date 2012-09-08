(ns virtualkeiko.web
  (:use ring.adapter.jetty)
  (:use virtualkeiko.handler))

(defn -main []
  (let [port (Integer/parseInt (System/getenv "PORT"))]
    (initalize-app)
    (run-jetty app {:port port})))
