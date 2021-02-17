(ns recordsapi.data
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.data.csv :as csv])
  (:import [java.time LocalDate]))

; Input file field order
(def fields [:last :first :email :color :birthdate])

(defn read-first-line
  "Read the first line of `path`"
  [path]
  (with-open [reader (io/reader path)]
    (first (line-seq reader))))

(defn detect-sep
  "Attempt to detect the record separator character used in the specified `line`"
  [line]
  (condp #(s/includes? %2 %1) line
    " | " \|
    ", " \,
    " " \space
    (throw (Exception. "Unknown record separator"))))

(defn parse-cols
  [cols]
  (flatten [(map s/trim (butlast cols))
            (-> cols last s/trim LocalDate/parse)]))

(defn read-file
  "Read CSV file from `path`, autodetecting the record separator"
  [path]
  (let [sep (-> path read-first-line detect-sep)]
    (with-open [reader (io/reader path)]
      (doall
        ; Trim any spaces from each record
        ;(map #(map s/trim %) (csv/read-csv reader :separator (or sep \,)))
        (map parse-cols
             (csv/read-csv reader :separator sep))))))

(defn mapify
  "Convert records into a map matching the expected input spec"
  [cols]
  (zipmap fields cols))

(defn read-data
  "Get fully parsed record maps from `path`"
  [path]
  (map mapify (read-file path)))


(comment

  (map mapify (read-file (io/resource "data.csv")))
  (read-file (io/resource "data.psv"))
  (read-file (io/resource "data.ssv"))


  )
