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
  (if (and (valid-name? name)
           (not (nil? key)))
    (do
      (insert! :keikos {:name name :key key :signal "000"})
      "OK")
    "invalid parameters"))

(defn new-keiko []
  (html5
   [:head [:title "create your keiko"]]
   [:body (form-to [:post "/create"]
                   (text-field :name)
                   (text-field :key)
                   (submit-button "submit"))]))

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

(defn update-keiko [name key signal]
  (if (not (valid-signal? signal))
    "invalid signal format"
    (let [keiko (get-keiko name)]
      (if (not (= (:key keiko) key))
        "invalid key"
        (do
          (update! keiko (merge keiko { :signal (new-signal (:signal keiko) signal) }))
          "OK")))))

(defroutes app-routes
  (GET "/" [] (list-keiko))
  (GET "/new" [] (new-keiko))
  (GET "/:name" [name] (:signal (get-keiko name)))
  (POST "/create" [name key] (make-keiko name key))
  (POST "/:name" [name key signal] (update-keiko name key signal))
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
