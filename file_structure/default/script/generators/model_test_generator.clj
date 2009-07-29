(ns generators.model-test-generator
  (:require [conjure.model.util :as util]
            [conjure.test.builder :as test-builder]
            [conjure.test.util :as test-util]
            [conjure.util.file-utils :as file-utils]))

(defn
#^{:doc "Prints out how to use the generate model test command."}
  usage []
  (println "You must supply a model name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj model-test <model>"))

(defn
#^{:doc "Generates the functional test file for the given controller and actions."}
  generate-unit-test [model]
  (let [unit-test-file (test-builder/create-model-unit-test model)]
    (if unit-test-file
      (let [test-namespace (test-util/model-unit-test-namespace model)
            model-namespace (util/model-namespace model)
            test-content (str "(ns " test-namespace "
  (:use clojure.contrib.test-is
        " model-namespace "))

(def model \"" model "\")

(deftest test-truth
  (is true))")]
        (file-utils/write-file-content unit-test-file test-content)))))

(defn 
#^{:doc "Generates a controller file for the controller name and actions in params."}
  generate-model-test [params]
  (generate-unit-test (first params)))