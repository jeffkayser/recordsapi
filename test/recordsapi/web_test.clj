(ns recordsapi.web-test
  (:require [clojure.test :refer :all]
            [ring.util.response :refer [response content-type]]
            [ring.mock.request :refer [request]]
            [recordsapi.data :refer [db]]
            [recordsapi.web :as web])
  (:import [java.time LocalDate]))

(def foo {:last "bar" :first "foo" :color "green" :email "me@hrm.com" :birthdate (LocalDate/of 1970 1 1)})
(def bar {:last "asdf" :first "qwerty" :color "red" :email "asdf@hrm.com" :birthdate (LocalDate/of 1966 12 31)})

(defn init-test-data [test-fn]
  (reset! db [foo bar])
  (test-fn))

(use-fixtures :once init-test-data)

(deftest wrap-content-type
  (testing "sets Content-Type header to JSON"
    (let [handler (-> (constantly (response "foo"))
                      web/wrap-content-type-json)
          resp (handler (request :get "/"))]
      (is (= resp {:status  200
                   :headers {"Content-Type" "application/json"}
                   :body    "foo"})))))

(deftest wrap-body-json
  (testing "returns body as JSON"
    (let [handler (-> (constantly (response {:foo "bar"
                                             :b 123}))
                      web/wrap-body-json)
          resp (handler (request :get "/"))]
      (is (= resp {:status  200
                   :headers {}
                   :body    "{\"foo\":\"bar\",\"b\":123}"})))))

(deftest add-record
  (testing "adds record"
    (is (= {:status 201
            :body   {:message "Record successfully added"
                     :success true}})
        (web/add-record "{\"line\": \"a,b,c,d,1000-01-01}"))

    (is (= {:status 400
            :body   {:message "Failed to process specified record"
                     :success false}})
        (web/add-record "{\"foo\": \"another bar?"))))

(deftest get-records-by
  (testing "gets properly sorted records"
    (is (= [bar foo] (get-in (web/get-records-by :email) [:body :records])))
    (is (= [bar foo] (get-in (web/get-records-by :birthdate) [:body :records])))
    (is (= [bar foo] (get-in (web/get-records-by :last) [:body :records])))))

(deftest routes
  (testing "routing and response are correct"
    (let [resp {:status 200 :headers {} :body {:message "<placeholder>"
                                               :success true
                                               :records (list bar foo)}}]
      ; get by email
      (is
        (= (assoc-in resp [:body :message] "Got records sorted by email")
           (web/routes {:uri "/records/email" :request-method :get})))

      ; get by birthdate
      (is
        (= (assoc-in resp [:body :message] "Got records sorted by birthdate")
           (web/routes {:uri "/records/birthdate" :request-method :get})))

      ; get by last name
      (is
        (= (assoc-in resp [:body :message] "Got records sorted by last")
           (web/routes {:uri "/records/name" :request-method :get})))

      ; bad URL
      (is
        (= {:status 404 :headers {} :body {:message "Not found"
                                           :success false}}
           (web/routes {:uri "/foo" :request-method :get})))
        )))
