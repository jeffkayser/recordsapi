(ns recordsapi.data-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [recordsapi.data :as data]))

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

(deftest read-file
  (testing "files read and parsed properly"
    (let [expected [["B" "A" "ab@foo.net" "red" "2001-02-03"]
                    ["Y" "X" "x.y@bar.org" "blue" "2010-01-31"]]]
      (is (= expected (data/read-file (io/resource "test/pipe.txt"))))
      (is (= expected (data/read-file (io/resource "test/comma.txt"))))
      (is (= expected (data/read-file (io/resource "test/space.txt")))))))

(deftest mapify
  (testing "creates map from fields/values"
    (is (= {:last "foo" :first "bar" :email "baz" :color 123 :birthdate :thud}
           (data/mapify ["foo" "bar" "baz" 123 :thud])))))

(deftest read-data
  (testing "file converted into record correctly"
    (is (= [{:last "B" :first "A" :email "ab@foo.net" :color "red" :birthdate "2001-02-03"}
            {:last "Y" :first "X" :email "x.y@bar.org" :color "blue" :birthdate "2010-01-31"}]
           (data/read-data (io/resource "test/pipe.txt"))))))