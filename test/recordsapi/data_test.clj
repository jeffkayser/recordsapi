(ns recordsapi.data-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [recordsapi.data :as data])
  (:import (java.time LocalDate)))

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

(deftest read-file
  (testing "files read and parsed properly"
    (let [expected [["B" "A" "ab@foo.net" "red" (LocalDate/of 2001 2 3)]
                    ["Y" "X" "x.y@bar.org" "blue" (LocalDate/of 2010 1 31)]]]
      (is (= expected (data/read-file (io/resource "test/pipe.txt"))))
      (is (= expected (data/read-file (io/resource "test/comma.txt"))))
      (is (= expected (data/read-file (io/resource "test/space.txt")))))))

(deftest mapify
  (testing "creates map from fields/values"
    (is (= {:last "foo" :first "bar" :email "baz" :color 123 :birthdate :thud}
           (data/mapify ["foo" "bar" "baz" 123 :thud])))))

(deftest read-data
  (testing "file converted into record correctly"
    (is (= [{:last "B" :first "A" :email "ab@foo.net" :color "red" :birthdate (LocalDate/of 2001 2 3)}
            {:last "Y" :first "X" :email "x.y@bar.org" :color "blue" :birthdate (LocalDate/of 2010 1 31)}]
           (data/read-data (io/resource "test/pipe.txt"))))))
