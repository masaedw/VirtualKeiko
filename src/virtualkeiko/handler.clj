(ns virtualkeiko.handler
  (:use compojure.core)
  (:use somnium.congomongo)
  (:use hiccup.core)
  (:use hiccup.form)
  (:use hiccup.page)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]))

(defn make-keiko-connection []
  (make-connection (get (System/getenv) "MONGOHQ_URL")))

(defn initialize-app []
  (set-connection! (make-keiko-connection)))

(defn valid-name? [name]
  (and (not (nil? name))
       (< (.length name) 50)
       (re-find #"^[^/]+$" name)))

;; (valid-name? "hogefuga")
;; (valid-name? "hoge/fuga")
;; (valid-name? "012345678901234567890123456789012345678901234567890123456789")

(defn make-keiko [name signal]
  (cond
   (not (valid-name? name)) "invalid name"
   (nil? key) "invalid key"
   :else (insert! :keikos {:name name :signal signal})))

(defn get-keiko [name]
  (fetch-one :keikos :where {:name name}))

(defn h-get-keiko [name]
  (format "%s\n" (or (:signal (get-keiko name)) "000")))

(defn h-usage []
  (html5
   [:head [:title "virtualkeiko"]]
   [:body
    [:h1 "virtualkeiko"]
    [:h2 "What's this"]
    [:p
     "This is a virtual keiko service.  Keiko is a series of great alert lamp devices which accepts to controll by rsh.  "
     "Every keikos have at least 3 lamps, which are red, yellow and green, and its status represented as 3 numbers."]
    [:h2 "get your keiko's status"]
    [:p "curl http://virtualkeiko.herokuapp.com/&lt;your keiko's name&gt;"]
    [:h2 "update your keiko's status"]
    [:p "curl -X POST -d 'signal=&lt;new signal&gt;' http://virtualkeiko.herokuapp.com/&lt;your keiko's name&gt;"]
    [:h2 "signal format"]
    [:p
     "Keiko's signal is represented as 3 numbers.  The first number is red lamp, next is yellow lamp and last is green lamp.  "
     "Lamps have 3 statuses; turned off (0), turned on (1), blinking (2)."]
    [:dl
     [:dt "000"] [:dd "all lamps are turned off"]
     [:dt "010"] [:dd "yellow lamp is turned on"]
     [:dt "022"] [:dd "yellow and green lamps are blinking"]]
    [:p "You can use 'X' as 'save the current status' in new signal string.  0X0 means 'turn off red and green, save yellow's status!'"]
    ]))

(defn valid-signal? [signal]
  (and (not (nil? signal))
       (re-find #"^[012xX]{3}" signal)))

(defn new-signal [old new]
  (let [select (fn [o n] (if (or (= n \x) (= n \X)) o n))]
    (apply str (map select old new))))

;; (new-signal "021" "x0X") ;=> "001"

(defn h-update-keiko [name signal]
  (cond
   (not (valid-name? name)) "invalid name"
   (not (valid-signal? signal)) "invalid signal format"
   :else (do
           (let [keiko (get-keiko name)]
             (if keiko
               (update! :keikos keiko (merge keiko { :signal (new-signal (:signal keiko) signal) }))
               (make-keiko name signal))
             "OK\n"))))

(defroutes app-routes
  (GET "/" [] (h-usage))
  (GET "/:name" [name] (h-get-keiko name))
  (POST "/:name" [name signal] (h-update-keiko name signal))
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
