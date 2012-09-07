(defproject virtualkeiko "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.3"]
                 [congomongo "0.1.9"]]
  :plugins [[lein-ring "0.7.5"]]
  :ring {:handler virtualkeiko.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
