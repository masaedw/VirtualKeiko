(defproject virtualkeiko "0.1.0-SNAPSHOT"
  :min-lein-version "2.0.0"
  :description "FIXME: write description"
  :url "http://virtualkeiko.herokuapp.com"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [congomongo "0.4.1"]
                 [hiccup "1.0.4"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [ring "1.2.0"]]
  :plugins [[lein-ring "0.8.6"]
            [lein-swank "1.4.5"]]
  :ring {:handler virtualkeiko.handler/app}
  :main virtualkeiko.web
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]]}
   :production {}})
