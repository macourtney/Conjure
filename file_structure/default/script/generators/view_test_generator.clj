(ns generators.view-test-generator
  (:require [conjure.test.builder :as test-builder]
            [conjure.test.util :as test-util]
            [conjure.util.file-utils :as file-utils]
            [conjure.view.util :as util]))

(defn
#^{:doc "Prints out how to use the generate test view command."}
  usage []
  (println "You must supply a controller and view name (Like hello-world show).")
  (println "Usage: ./run.sh script/generate.clj view-test <controller> <action>"))

(defn
#^{:doc "Generates the functional test file for the given controller and actions."}
  generate-unit-test 
  ([controller action] (generate-unit-test controller action false))
  ([controller action silent]
  (let [unit-test-file (test-builder/create-view-unit-test controller action silent)]
    (if unit-test-file
      (let [test-namespace (test-util/view-unit-test-namespace controller action)
            view-namespace (util/view-namespace-by-action controller action)
            test-content (str "(ns " test-namespace "
  (:use clojure.contrib.test-is
        " view-namespace "))

(def controller-name \"" controller "\")
(def view-name \"" action "\")
(def request-map { :controller controller-name
                   :action view-name } )

(deftest test-view
  (render-view request-map))")]
        (file-utils/write-file-content unit-test-file test-content))))))

(defn 
#^{:doc "Generates a controller file for the controller name and actions in params."}
  generate-view-test [params]
  (generate-unit-test (first params) (second params)))