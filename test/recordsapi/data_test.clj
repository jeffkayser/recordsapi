(ns recordsapi.data-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [recordsapi.data :as data])
  (:import (java.time LocalDate)))

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

(def pipe "B | A | ab@foo.net | red | 2001-02-03\r\nY | X | x.y@bar.org | blue | 2010-01-31\r\n")
(def comma "B, A, ab@foo.net, red, 2001-02-03\nY, X, x.y@bar.org, blue, 2010-01-31")
(def space "B A ab@foo.net red 2001-02-03\nY X x.y@bar.org blue 2010-01-31\n")


(defn init-test-data [test-fn]
  (reset! data/db [bob foo frank])
  (test-fn))

(use-fixtures :once init-test-data)

(deftest get-records
  (testing "records are sorted to spec"
    ; email desc, last name asc
    (is (= [foo bob frank] (data/get-records 1)))

    ; birthdate
    (is (= [frank foo bob] (data/get-records 2)))

    ; last name desc
    (is (= [frank bob foo] (data/get-records 3)))))

(deftest fields
  (testing "data format is correctly defined"
    (is (= [:last :first :email :color :birthdate] data/fields))))

(deftest read-first-line
  (testing "reads a single line from a text file"
    (is (= "a, | z" (data/read-first-line (io/resource "test/read.txt"))))))

(deftest detect-sep
  (testing "autodetects record separator"
    (is (= \| (data/detect-sep "a | b | c")))
    (is (= \, (data/detect-sep "a, b, c")))
    (is (= \space (data/detect-sep "a b c")))
    (is (thrown-with-msg? Exception #"Unknown record separator" (data/detect-sep "a\tb\tc")))))

(deftest parse-line
  (testing "parse line properly"
    ;(not (is (clojure.string/includes? " ")))
    (is (= ["asdf" (LocalDate/of 1988 3 15)]
          (data/parse-cols [" asdf " " 1988-03-15"]))) ) )

(deftest parse-string
  (testing "input strings parsed properly")
  (let [expected [["B" "A" "ab@foo.net" "red" (LocalDate/of 2001 2 3)]
                  ["Y" "X" "x.y@bar.org" "blue" (LocalDate/of 2010 1 31)]]]
    (is (= expected (data/parse-string pipe)))
    (is (= expected (data/parse-string comma)))
    (is (= expected (data/parse-string space)))))

(deftest mapify
  (testing "creates map from fields/values"
    (is (= {:last "foo" :first "bar" :email "baz" :color 123 :birthdate :thud}
           (data/mapify ["foo" "bar" "baz" 123 :thud])))))

(deftest read-data
  (testing "file converted into record correctly"
    (is (= [{:last "B" :first "A" :email "ab@foo.net" :color "red" :birthdate (LocalDate/of 2001 2 3)}
            {:last "Y" :first "X" :email "x.y@bar.org" :color "blue" :birthdate (LocalDate/of 2010 1 31)}]
           (data/read-data (io/resource "test/pipe.txt"))))))
