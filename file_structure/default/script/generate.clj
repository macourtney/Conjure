(ns generate 
  (:use [generators.migration-generator :as migration-generator]))

(defn print-usage []
  (println "Usage: ./run.sh script/Generate.clj <generate type> <generate params>*"))

(defn print-unknown-command [command]
  (println (str "Unknown command: " command))
  (print-usage))

(defn generate [command params]
  (if (. command equals "migration")
    (migration-generator/generate-migration params)
    (print-unknown-command command)))

(let [command (first *command-line-args*)
      generate-args (rest *command-line-args*)]
  (if command
    (let [generate-command (first generate-args)
          generate-type-params (rest generate-args)]
      (if generate-command
        (generate generate-command generate-type-params)

        (print-usage)))

    (do
      (println "Nil command, cannot continue.")
      (print-usage))))