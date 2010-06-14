(ns conjure-script.generate 
  (:require [conjure.server.server :as server]))

(defn print-usage []
  (println "Usage: ./run.sh script/generate.clj <generate type> <generate params>*"))

(defn print-unknown-command [command]
  (println (str "Unknown command: " command))
  (print-usage))

(defn print-invalid-generator [generator-namspace]
  (println (str "Invalid generator: " generator-namspace ". The generator must implement a generate function.")))

(defn generate [command params]
  (let [generator-namspace-symbol (symbol (str "generators." command "-generator"))]
    (require generator-namspace-symbol)
    (let [generator-namespace (find-ns generator-namspace-symbol)]
      (if generator-namespace
        (let [generator-fn (ns-resolve generator-namespace 'generate)]
          (if generator-fn
            (generator-fn params)
            (print-invalid-generator generator-namespace)))
        (print-unknown-command command)))))

(server/init)

(let [generate-command (first *command-line-args*)
      generate-type-params (rest *command-line-args*)]
  (if generate-command
    (generate generate-command generate-type-params)
    (print-usage)))