(ns generators.controller-test-generator
  (:require [clojure.contrib.str-utils :as str-utils]
            [conjure.controller.util :as util]
            [conjure.test.builder :as test-builder]
            [conjure.test.util :as test-util]
            [conjure.util.file-utils :as file-utils]))

(defn
#^{:doc "Prints out how to use the generate test controller command."}
  controller-usage []
  (println "You must supply a controller name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj controller-test <controller> [action]*"))

(defn
  generate-action-test-function [action]
  (str "(deftest test-" action "
  (" action " { :controller controller-name :action \"" action "\" }))"))

(defn
#^{:doc "Generates the action functions block for a functional test file."}
  generate-all-action-tests [actions]
  (str-utils/str-join "\n\n" (map generate-action-test-function actions)))

(defn
#^{:doc "Generates the functional test file for the given controller and actions."}
  generate-functional-test 
  ([controller actions] (generate-functional-test controller actions false))
  ([controller actions silent]
    (let [functional-test-file (test-builder/create-functional-test controller silent)]
      (if functional-test-file
        (let [test-namespace (test-util/functional-test-namespace controller)
              controller-namespace (util/controller-namespace controller)
              test-content (str "(ns " test-namespace "
  (:use clojure.contrib.test-is
        " controller-namespace "))

(def controller-name \"" controller "\")

" (generate-all-action-tests actions))]
          (file-utils/write-file-content functional-test-file test-content))))))
        
(defn 
#^{:doc "Generates a controller file for the controller name and actions in params."}
  generate-controller-test [params]
  (generate-functional-test (first params) (rest params)))