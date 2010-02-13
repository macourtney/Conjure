(ns generators.fixture-generator
  (:require [conjure.model.util :as model-util]
            [conjure.test.builder :as test-builder]
            [conjure.test.util :as test-util]
            [conjure.util.file-utils :as file-utils]))

(defn
#^{:doc "Prints out how to use the generate fixture command."}
  usage []
  (println "You must supply a model name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj fixture <model>"))

(defn
#^{:doc "Generates the fixture file for the given model."}
  generate-fixture-file
  ([model silent]
    (let [fixture-file (test-builder/create-fixture model silent)]
      (if fixture-file
        (let [fixture-namespace (test-util/fixture-namespace model)
              table-name (model-util/model-to-table-name model)
              fixture-content (str "(ns " fixture-namespace "
  (:use conjure.model.database))

(def records [
  ; Add your test data here.
  { :id 1 }])

(defn fixture [function]
  (apply insert-into :" table-name " records)
  (function)
  (delete :" table-name " [ \"true\" ]))")]
        (file-utils/write-file-content fixture-file fixture-content))))))

(defn 
#^{:doc "Generates a fixture file for the model name in params."}
  generate-fixture [params]
  (generate-fixture-file (first params) false))