(ns conjure.script.generators.flow-generator
  (:require [clojure.tools.logging :as logging]
            [clojure.string :as str-utils]
            [conjure.flow.builder :as builder]
            [conjure.flow.util :as util]
            [clojure.tools.file-utils :as file-utils]
            [conjure.script.generators.flow-test-generator :as flow-test-generator]))

(defn
#^{ :doc "Prints out how to use the generate flow command." }
  usage []
  (println "You must supply a service name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj flow <service> [action]*"))
  
(defn
#^{ :doc "Generates the action function for the given action." }
  generate-action-function [action]
  (str "(def-action " action "
  (bind))"))
  
(defn
#^{ :doc "Generates the action functions block for a flow file." }
  generate-all-action-functions [actions]
  (str-utils/join "\n\n" (map generate-action-function actions)))

(defn
#^{ :doc "Generates the content of the given flow file." }
  generate-flow-content 
  ([service flow-content] (generate-flow-content service flow-content nil))
  ([service flow-content requires]
    (str "(ns " (util/flow-namespace service) "
  (:use [conjure.flow.base])" (when requires (str "\n  (:require " requires ")")) ")

" flow-content)))

(defn
#^{ :doc "Creates a flow file with the given File and file content." }
  create-flow-files 
  [{ :keys [service flow-content actions silent] :or { silent false } }]
    (if-let [flows-directory (util/find-flows-directory)]
      (do
        (when-let [flow-file (builder/create-flow-file 
                                { :service service, 
                                  :flows-directory flows-directory, 
                                  :silent silent })]
          (file-utils/write-file-content flow-file flow-content))
        (flow-test-generator/generate-functional-test service actions silent))
      (logging/error "Could not find the flows directory")))

(defn
#^{ :doc "Generates the flow content and saves it into the given flow file." }
  generate-file-content 
  [{ :keys [service actions silent] :or { silent false } }]
    (create-flow-files
      { :service service
        :flow-content (generate-flow-content service (generate-all-action-functions actions))
        :actions actions
        :silent silent }))

(defn
#^{ :doc "Creates the flow file associated with the given service." }
  generate-flow-file
  [{ :keys [service actions silent] :or { actions (), silent false } }]
    (if (and service actions)
      (generate-file-content { :service service, :actions actions, :silent silent })
      (when (not silent)
        (usage))))

(defn 
#^{ :doc "Generates a flow file for the service name and actions in params." }
  generate [params]
  (generate-flow-file { :service (first params), :actions (rest params) }))