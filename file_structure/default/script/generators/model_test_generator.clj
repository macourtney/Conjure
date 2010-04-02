(ns generators.model-test-generator
  (:require [conjure.model.util :as util]
            [conjure.test.builder :as test-builder]
            [conjure.test.util :as test-util]
            [conjure.util.file-utils :as file-utils]
            [generators.fixture-generator :as fixture-generator]))

(defn
#^{:doc "Prints out how to use the generate model test command."}
  usage []
  (println "You must supply a model name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj model-test <model>"))

(defn
#^{:doc "Generates the unit test file for the given model."}
  generate-unit-test 
  ([model] (generate-unit-test model false))
  ([model silent]
    (let [unit-test-file (test-builder/create-model-unit-test model silent)]
      (if unit-test-file
        (let [test-namespace (test-util/model-unit-test-namespace model)
              model-namespace (util/model-namespace model)
              fixture-namespace (test-util/fixture-namespace model)
              test-content (str "(ns " test-namespace "
    (:use clojure.contrib.test-is
          " model-namespace "
          " fixture-namespace "))
  
  (def model \"" model "\")
  
  (use-fixtures :once fixture)
  
  (deftest test-first-record
    (is (get-record 1)))")]
          (file-utils/write-file-content unit-test-file test-content)
          (fixture-generator/generate-fixture-file model silent))))))

(defn 
#^{:doc "Generates a model test file for the model name in params."}
  generate [params]
  (generate-unit-test (first params)))