(ns generators.binding-test-generator
  (:require [clojure.contrib.str-utils :as str-utils]
            [conjure.test.builder :as test-builder]
            [conjure.test.util :as test-util]
            [conjure.util.file-utils :as file-utils]))

(defn
#^{:doc "Prints out how to use the generate test binding command."}
  usage []
  (println "You must supply a controller name (Like hello-world) and action name (like show-me).")
  (println "Usage: ./run.sh script/generate.clj binding-test <controller> <action>"))

(defn
#^{:doc "Generates the binding unit test file for the given controller and action."}
  generate-unit-test 
  ([controller action] (generate-unit-test controller action false))
  ([controller action silent] (generate-unit-test controller action silent nil))
  ([controller action silent incoming-content]
  (let [unit-test-file (test-builder/create-view-unit-test controller action silent)]
    (if unit-test-file
      (let [test-namespace (test-util/binding-unit-test-namespace controller action)
            test-content (or incoming-content (str "(ns " test-namespace "
  (:use clojure.contrib.test-is)
  (:require [conjure.binding.util :as bind-util]))

(def controller-name \"" controller "\")
(def action-name \"" action "\")

(deftest test-view
  (bind-util/call-binding controller-name action-name))"))]
        (file-utils/write-file-content unit-test-file test-content))))))
        
(defn 
#^{:doc "Generates a binding test file for the controller and action in params."}
  generate [params]
  (generate-unit-test (first params) (rest params)))