(ns virtualkeiko.web
  (:use ring.adapter.jetty)
  (:use ring.middleware.reload)
  (:use virtualkeiko.handler)
  (:use [clojure.tools.nrepl.server :only (start-server)])
  )

(defn -main []
  (let [nrepl-port (System/getenv "NREPL_PORT")
        port (Integer/parseInt (or (System/getenv "PORT") "8080"))
        reload (System/getenv "RELOAD")]
    (if nrepl-port
      (start-server :port (Integer/parseInt nrepl-port)))
    (let [app (if reload
                (wrap-reload app '(virtualkeiko.handler))
                app)]
      (initialize-app)
      (run-jetty app {:port port}))))
