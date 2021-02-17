(ns recordsapi.core
  (:require [clojure.pprint :as pp]
            [clojure.string :as s]
            [recordsapi.cli :as cli]
            [recordsapi.data :as data])
  (:import [java.time.format DateTimeFormatter])
  (:gen-class))

; Output date format
(def date-format (DateTimeFormatter/ofPattern "M/d/yyyy"))

; Our record "database"; a vector of maps
(defonce db (atom []))

(defn add-record!
  "Add record to 'database' atom"
  [rec]
  (swap! db conj rec))

(defn load-data!
  "Load data from path into 'database'"
  [path]
  (let [lines (data/read-data path)]
    (doall
      (map add-record! lines))))

(defn run-server [port]
  (throw (Exception. "Not implemented yet")))


(defn get-records
  "Get records sorted by specified mode"
  [view-mode]
  (condp = view-mode
    1 (sort-by :email #(compare %2 %1) (sort-by :last @db)) ; Email descending, then last name
    2 (sort-by :birthdate @db)                              ; Birth date
    3 (sort-by :last #(compare %2 %1) @db)                  ; Last name descending
    (throw (Exception. (str "Invalid view option: '" view-mode "'")))))

(defn format-record
  "Format record per specification"
  [record]
  (into record {:birthdate (.format (:birthdate record) date-format)}))

(defn format-output
  "Trim and left-justifies ASCII table"
  [str]
  (-> str
      s/trim
      (s/replace #"\| (\s*)([^\s]+)" "| $2$1")))

(defn print-records
  "Output records in final, user-friendly format"
  [records]
  (->> records
       (map format-record)
       pp/print-table
       with-out-str
       format-output
       println))

(defn -main [& args]
  (let [{:keys [options exit-message ok?]} (cli/validate-args args)]
    (prn options)
    (when exit-message
      (cli/exit (if ok? 0 1) exit-message))
    (when (:file options)
      (load-data! (:file options)))
    (if (:server options)
      (run-server (:port options))
      (print-records (get-records (:view options))))))
  )
