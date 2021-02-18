(ns recordsapi.data
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.data.csv :as csv])
  (:import [java.time LocalDate]))

; Input file field order
(def fields [:last :first :email :color :birthdate])

; Our record "database"; a vector of maps
(defonce db (atom []))

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
  "Parse string cols into desired storage data type"
  [cols]
  (flatten [(map s/trim (butlast cols))
            (-> cols last s/trim LocalDate/parse)]))

(defn parse-string
  "Parse input CSV from `str`, autodetecting the record separator"
  [str]
  (let [first-line (s/split str #"\r?\n" 2)
        sep (-> first-line detect-sep)]
    (map parse-cols (csv/read-csv str :separator sep))))

(defn mapify
  "Convert records into a map matching the expected input spec"
  [cols]
  (zipmap fields cols))

(defn read-data
  "Get fully parsed record maps from `path`"
  [path]
  (map mapify (parse-string (slurp path))))

(defn add-record!
  "Add record to 'database' atom"
  [rec]
  (swap! db conj rec))

(defn load-data!
  "Load data from path into 'database'"
  [path]
  (let [lines (read-data path)]
    (doall
      (map add-record! lines))))

(defn get-records
  "Get records sorted by specified mode"
  [view-mode]
  (condp = view-mode
    1 (sort-by :email #(compare %2 %1) (sort-by :last @db)) ; Email descending, then last name
    2 (sort-by :birthdate @db)                              ; Birthdate
    3 (sort-by :last #(compare %2 %1) @db)                  ; Last name descending
    (throw (Exception. (str "Invalid view option: '" view-mode "'")))))


(comment

  (map mapify (parse-string (slurp (io/resource "data.csv"))))
  (parse-string (slurp (io/resource "data.psv")))
  (parse-string (slurp (io/resource "data.ssv")))


  )
