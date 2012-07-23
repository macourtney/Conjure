(ns conjure.script.generators.flow-test-generator
  (:require [clojure.string :as str-utils]
            [conjure.flow.util :as util]
            [conjure.test.builder :as test-builder]
            [conjure.test.util :as test-util]
            [clojure.tools.file-utils :as file-utils]))

(defn
#^{:doc "Prints out how to use the generate test flow command."}
  usage []
  (println "You must supply a service name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj flow-test <service> [action]*"))

(defn
  generate-action-test-function [action]
  (str "(deftest test-" action "
  (is (flow-util/call-service { :service service-name :action \"" action "\" })))"))

(defn
#^{:doc "Generates the action functions block for a functional test file."}
  generate-all-action-tests [actions]
  (str-utils/join "\n\n" (map generate-action-test-function actions)))

(defn generate-test-content
  [service actions]
  (let [test-namespace (test-util/functional-test-namespace service)
        flow-namespace (util/flow-namespace service)]
    (str "(ns " test-namespace "
      (:use clojure.test
            " flow-namespace ")
      (:require [conjure.flow.util :as flow-util]))
    
    (def service-name \"" service "\")
    
    " (generate-all-action-tests actions))))

(defn
#^{:doc "Generates the functional test file for the given service and actions."}
  generate-functional-test 
  ([service actions] (generate-functional-test service actions false))
  ([service actions silent]
    (when-let [functional-test-file (test-builder/create-functional-test service silent)]
      (file-utils/write-file-content functional-test-file (generate-test-content service actions)))))

(defn 
#^{:doc "Generates a flow file for the service name and actions in params."}
  generate [params]
  (generate-functional-test (first params) (rest params)))