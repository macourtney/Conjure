(ns generate 
  (:require [conjure.server.server :as server]
            [generators.binding-generator :as binding-generator]
            [generators.controller-generator :as controller-generator]
            [generators.controller-test-generator :as controller-test-generator]
            [generators.fixture-generator :as fixture-generator]
            [generators.migration-generator :as migration-generator]
            [generators.model-generator :as model-generator]
            [generators.model-test-generator :as model-test-generator]
            [generators.scaffold-generator :as scaffold-generator]
            [generators.view-generator :as view-generator]
            [generators.view-test-generator :as view-test-generator]
            [generators.xml-view-generator :as xml-view-generator]))

(defn print-usage []
  (println "Usage: ./run.sh script/generate.clj <generate type> <generate params>*"))

(defn print-unknown-command [command]
  (println (str "Unknown command: " command))
  (print-usage))

(defn print-invalid-generator [generator-namspace]
  (println (str "Invalid generator: " generator-namspace ". The generator must implement a generate function.")))

(defn generate [command params]
  (let [generator-namespace (find-ns (symbol (str "generators." command)))]
    (if generator-namespace)
      (let [generator-fn (ns-resolve generator-namespace 'generate)]
        (if generator-fn
          (generator-fn params)
          (print-invalid-generator generator-namespace)))
      (print-unknown-command command)))

(server/init)

(let [generate-command (first *command-line-args*)
      generate-type-params (rest *command-line-args*)]
  (if generate-command
    (generate generate-command generate-type-params)
    (print-usage)))