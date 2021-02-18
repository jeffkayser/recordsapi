(ns recordsapi.core
  (:require [clojure.pprint :as pp]
            [clojure.string :as s]
            [recordsapi.cli :as cli]
            [recordsapi.data :as data :refer [db]]
            [recordsapi.web :as web])
  (:import [java.time.format DateTimeFormatter])
  (:gen-class))

; Output date format
(def date-format (DateTimeFormatter/ofPattern "M/d/yyyy"))

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
    (when exit-message
      (cli/exit (if ok? 0 1) exit-message))
    (when (:file options)
      (data/load-data-from-file! (:file options)))
    (if (:server options)
      (web/start-server (:port options))
      (print-records (data/get-records (:view options))))))


(comment

  (load-data! (clojure.java.io/resource "data.csv"))
  (count @db)
  @db
  (reset! db [])

  (defn output-line [line]
    (flatten [(butlast (vals line)) (.format (last (vals line)) date-format)])
    )

  (map (comp load-data! clojure.java.io/resource) ["data.csv" "data.psv" "data.ssv"])


  )
