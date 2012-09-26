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

(defn initalize-app []
  (set-connection! (make-keiko-connection)))

(defn valid-name? [name]
  (and (not (nil? name))
       (< (.length name) 50)
       (re-find #"^[^/]+$" name)))

;; (valid-name? "hogefuga")
;; (valid-name? "hoge/fuga")
;; (valid-name? "012345678901234567890123456789012345678901234567890123456789")

(defn make-keiko [name key]
  (cond
   (not (valid-name? name)) "invalid name"
   (nil? key) "invalid key"
   (< 0 (fetch-count :keikos :where {:name name})) (str name " already exists")
   :else (do
           (insert! :keikos {:name name :key key :signal "000"})
           "OK")))

(defn new-keiko []
  (html5
   [:head [:title "create your keiko"]]
   [:body (form-to [:post "/create"]
                   (text-field :name)
                   (text-field :key)
                   (submit-button "submit"))]))

(defn get-keiko [name]
  (fetch-one :keikos :where {:name name}))

(defn usage []
  (html5
   [:head [:title "virtualkeiko"]]
   [:body
    [:h1 "virtualkeiko"]
    [:h2 "What's this"]
    [:p
     "This is a virtual keiko service.  Keiko is a series of great alert lamp devices which accepts to controll by rsh.  "
     "Every keikos have at least 3 lamps, which are red, yellow and green, and its status represented as 3 numbers."]
    [:h2 "create new virtual keiko"]
    [:p "curl -X POST -d 'name=&lt;your keiko's name&gt;&amp;key=&lt;update password&gt;' http://virtualkeiko.herokuapp.com/create"]
    [:h2 "get your keiko's status"]
    [:p "curl http://virtualkeiko.herokuapp.com/&lt;your keiko's name&gt;"]
    [:h2 "update your keiko's status"]
    [:p "curl -X POST -d 'key=&lt;update password&gt;&amp;signal=&lt;new signal&gt;' http://virtualkeiko.herokuapp.com/&lt;your keiko's name&gt;"]
    [:h2 "signal format"]
    [:p
     "Keiko's signal is represented as 3 numbers.  The first number is red lamp, next is yellow lamp and last is green lamp.  "
     "Lamps has 3 states; turned off (0), turned on (1), blinking (2)."]
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

(defn update-keiko [name key signal]
  (cond
   (not (valid-name? name)) "invalid name"
   (not (valid-signal? signal)) "invalid signal format"
   (nil? key) "invalid key"
   :else (do
           (let [keiko (get-keiko name)]
             (cond
              (not (= (:key keiko) key)) "invalid key"
              :else
              (do
                (update! :keikos keiko (merge keiko { :signal (new-signal (:signal keiko) signal) }))
                "OK"))))))

(defroutes app-routes
  (GET "/" [] (usage))
  (GET "/new" [] (new-keiko))
  (GET "/:name" [name] (:signal (get-keiko name)))
  (POST "/create" [name key] (make-keiko name key))
  (POST "/:name" [name key signal] (update-keiko name key signal))
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
