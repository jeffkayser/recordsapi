(ns recordsapi.web
  (:require [compojure.core :refer [defroutes context GET POST]]
            [compojure.route :as route]
            [jsonista.core :as json]
            [ring.adapter.jetty :as jet]
            [recordsapi.data :as data :refer [db]]))

(defn wrap-content-type-json [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc-in response [:headers "Content-Type"] "application/json"))))

(defn wrap-body-json [handler]
  (fn [request]
    (let [response (handler request)]
      (assoc response :body (json/write-value-as-string (:body response))))))

(defn add-record
  [raw]
  (try
    (let [line (-> raw json/read-value (get "line"))]
      (data/load-data-from-string! line))
    {:status 201
     :body   {:message "Record successfully added"
              :success true}}
    (catch Exception e
      {:status 400
       :body   {:message "Failed to process specified record"
                :success false}})))

(defn get-records-by [sort-field]
  {:status 200
   :body   {:message (str "Got records sorted by " (name sort-field))
            :success true
            :records (sort-by sort-field @db)}})

(defroutes routes
  (context "/records" []
    (POST "/" {body :body} (add-record (slurp body)))
    (GET "/email" [] (fn [_] (get-records-by :email)))
    (GET "/birthdate" [] (fn [_] (get-records-by :birthdate)))
    (GET "/name" [] (fn [_] (get-records-by :last))))
    (route/not-found {:status 404
                      :body   {:message "Not found"
                               :success false}}))

(def app
  (-> routes
      wrap-body-json
      wrap-content-type-json))

(defn start-server [port]
  (jet/run-jetty app {:port  port
                      :join? false}))



(comment

  (wrap-content-type-json {:uri "/"} {:body ""})

  (json/write-value-as-string {:a "foo"})
  (json/read-value "{\"a\": \"foo\"}")

  )
