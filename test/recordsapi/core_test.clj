(ns recordsapi.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as s]
            [recordsapi.core :as core])
  (:import [java.time LocalDate]))

(def bob {:last      "Jenkins"
          :first     "Bob"
          :email     "quux@example.com"
          :color     "fuscia"
          :birthdate (LocalDate/of 2011 11 30)})

(def foo {:last      "Bar"
          :first     "Foo"
          :email     "quux@example.com"
          :color     "fnord"
          :birthdate (LocalDate/of 1934 3 9)})

(def frank {:last      "Stein"
            :first     "Franken"
            :email     "frank@monsters.net"
            :color     "green"
            :birthdate (LocalDate/of 1818 5 12)})

(defn init-test-data [test-fn]
  (reset! core/db [bob foo frank])
  (test-fn))

(use-fixtures :once init-test-data)

(deftest get-records
  (testing "records are sorted to spec"
    ; email desc, last name asc
    (is (= [foo bob frank] (core/get-records 1)))

    ; birthdate
    (is (= [frank foo bob] (core/get-records 2)))

    ; last name desc
    (is (= [frank bob foo] (core/get-records 3)))))

(deftest format-record
  (testing "formats birthdate properly, changes nothing else"
    (is (= {:f         "foo"
            :birthdate "1/2/2000"}
           (core/format-record {:f         "foo"
                                :birthdate (LocalDate/of 2000 1 2)})))
    (is (= {:b         "bar"
            :birthdate "1/22/2000"}
           (core/format-record {:b         "bar"
                                :birthdate (LocalDate/of 2000 1 22)})))))

(deftest format-output
  (testing "tables are formatted properly"
    (is (= "| a   | asdf  | baz |"
           (core/format-output
             "\n  |   a |  asdf | baz |   \t \n\r"))
        )))

(deftest print-records
  (testing "records are output per spec"
    (is (=
          (with-out-str
            (core/print-records
              [{:a "A" :b "B!" :birthdate (LocalDate/of 1986 5 13)}]))
          (s/join "\n" ["| :a | :b | :birthdate |"
                        "|----+----+------------|"
                        "| A  | B! | 5/13/1986  |\n"])))))
