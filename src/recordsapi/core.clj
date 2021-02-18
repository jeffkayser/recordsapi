(ns recordsapi.core
  (:require [clojure.pprint :as pp]
            [clojure.string :as s]
            [recordsapi.cli :as cli]
            [recordsapi.data :as data :refer [db]])
  (:import [java.time.format DateTimeFormatter])
  (:gen-class))

; Output date format
(def date-format (DateTimeFormatter/ofPattern "M/d/yyyy"))


(defn run-server [port]
  (throw (Exception. "Not implemented yet")))

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
      (run-server (:port options))
      (print-records (data/get-records (:view options))))))
  )
