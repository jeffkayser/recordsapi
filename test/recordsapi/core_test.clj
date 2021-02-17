(ns recordsapi.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as s]
            [recordsapi.core :as core])
  (:import [java.time LocalDate]))

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
