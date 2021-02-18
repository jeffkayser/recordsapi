(ns recordsapi.cli
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.tools.cli :refer [parse-opts]]))

(def cli-options
  [["-f" "--file INPUT" "Input file path"
    :parse-fn io/file
    :validate [#(.exists %) "File not found"]]
   ["-v" "--view TYPE" "Output view (ignored in \"web server\" mode). Sorts by: 1=email descending (default), last name; 2=birthdate; 3=last name descending"
    :parse-fn #(Integer/parseInt %)
    :validate [#(<= 1 % 3)]
    :default 1]
   ["-s" "--server" "Run API web server"
    :default false]
   ["-p" "--port PORT" "Web server port"
    :default 3030
    :parse-fn #(Integer/parseInt %)
    :validate [#(<= 1 % 65535) "Must be between 1 and 65535, inclusive"]]
   ["-h" "--help" "Show this help screen"]])

(defn usage [options-summary]
  (->> ["Records API"
        ""
        "Usage: lein run -- [options]"
        ""
        "Options:"
        options-summary
        ""]
       (s/join "\n")))

(defn error-msg [errors]
  (str "Errors occurred while parsing your command:\n\n"
       (s/join "\n" errors)))

(defn validate-args [args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-options)]
    (cond
      ; Help requested; show usage
      (:help options) {:exit-message (usage summary) :ok? true}

      ; Problem parsing; show errors
      errors {:exit-message (error-msg errors)}

      ; Args parsed correctly
      :else {:options options})))

(defn exit [status msg]
  (println msg)
  (System/exit status))


(comment
  (parse-opts ["--file" "/tmp/foo.txt" "--view" "2"] cli-options)

  (validate-args ["--file /tmp/foo.txt" "--server" "-p1234"])
  )
