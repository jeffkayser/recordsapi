(ns recordsapi.cli-test
  (:require [clojure.test :refer :all]
            [clojure.string :as s]
            [clojure.tools.cli :refer [parse-opts]]
            [recordsapi.cli :as cli]))

(deftest cli-options
  (testing "CLI options spec is a vec of vecs"
    ; Just a very basic structure check -- tools.cli already validates strictly via spec
    (is (vector? cli/cli-options))
    (is (every? true? (map vector? cli/cli-options)))))

(deftest usage
  (testing "usage screen is a string"
    (is (string? (cli/usage [])))))

(deftest error-msg
  (testing "error message screen includes errors"
    (let [errors ["FOO!" "and BAR!"]]
      (is (map #(s/includes? (cli/error-msg errors) %) errors)))))

(defn fails-validation? [args]
  (let [bad (cli/validate-args args)]
    (is (contains? bad :exit-message))
    (is (not= true (:ok? bad)))))

(deftest validate-args
  (testing "args are validated as expected"
    (let [help (cli/validate-args ["--help"])]
      (is (contains? help :exit-message))
      (is (= true (:ok? help))))

    (fails-validation? ["--asdf"])
    (fails-validation? ["--view 0"])
    (fails-validation? ["--view 4"])
    (fails-validation? ["--port 0"])
    (fails-validation? ["--port 65536"])
    (fails-validation? ["--port foo"])
    (fails-validation? ["--file /some/place/that/probably/does/not/exist.txt"])

    (let [good (:options (cli/validate-args ["-s" "-v2" "--file" "/dev/null" "--port" "65535"]))]
      (is (= true (:server good)))
      (is (= 2 (:view good)))
      (is (= (clojure.java.io/file "/dev/null") (:file good)))
      (is (= 65535 (:port good))))))
