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

(set-connection! (make-keiko-connection))

(defn valid-name? [name]
  (and (< (.length name) 50)
       (re-find #"^[^/]+$" name)))

;; (valid-name? "hogefuga")
;; (valid-name? "hoge/fuga")
;; (valid-name? "012345678901234567890123456789012345678901234567890123456789")

(defn make-keiko [name key]
  (if (valid-name? name)
    (do
      (insert! :keikos {:name name :key key :signal "000"})
      "OK")
    "invalid name"))

(defn new-keiko []
  (html5
   [:head [:title "create your keiko"]]
   [:body (form-to [:post "/create"]
                   (text-field :name)
                   (text-field :key))]))

(defn get-keiko [name]
  (fetch-one :keikos :where {:name name}))

(defn list-keiko []
  (html5
   [:head [:title "all keikos"]]
   [:body
    [:ul
     (for [keiko (fetch :keikos)]
       [:li [:span (:name keiko)] ":" [:span (:signal keiko)]])]]))

(defn valid-signal? [signal]
  (re-find #"^[012xX]{3}" signal))

(defn new-signal [old new]
  (let [select (fn [o n] (if (or (= n \x) (= n \X)) o n))]
    (apply str (map select old new))))

;; (new-signal "021" "x0X") ;=> "001"

(defn update-keiko [name signal]
  (if (not (valid-signal? signal))
    "invalid signal format"
    (let [keiko (get-keiko name)]
      (update! keiko (merge keiko { :signal (new-signal (:signal keiko) signal) }))
      "OK")))

(defroutes app-routes
  (GET "/" [] (list-keiko))
  (GET "/new" [] (new-keiko))
  (GET "/:name" [name] (:signal (get-keiko name)))
  (POST "/:name" [name signal] (update-keiko name signal))
  (POST "/create" [name key] (make-keiko name key))
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
