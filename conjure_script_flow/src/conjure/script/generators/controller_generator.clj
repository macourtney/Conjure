(ns conjure.script.generators.controller-generator
  (:require [clojure.tools.logging :as logging]
            [clojure.string :as str-utils]
            [conjure.flow.builder :as builder]
            [conjure.flow.util :as util]
            [clojure.tools.file-utils :as file-utils]
            [conjure.script.generators.controller-test-generator :as controller-test-generator]))

(defn
#^{ :doc "Prints out how to use the generate controller command." }
  controller-usage []
  (println "You must supply a controller name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj controller <controller> [action]*"))
  
(defn
#^{ :doc "Generates the action function for the given action." }
  generate-action-function [action]
  (str "(def-action " action "
  (bind))"))
  
(defn
#^{ :doc "Generates the action functions block for a controller file." }
  generate-all-action-functions [actions]
  (str-utils/join "\n\n" (map generate-action-function actions)))

(defn
#^{ :doc "Generates the content of the given controller file." }
  generate-controller-content 
  ([controller controller-content] (generate-controller-content controller controller-content nil))
  ([controller controller-content requires]
    (str "(ns " (util/controller-namespace controller) "
  (:use [conjure.core.controller.base])" (when requires (str "\n  (:require " requires ")")) ")

" controller-content)))

(defn
#^{ :doc "Creates a controller file with the given File and file content." }
  create-controller-files 
  [{ :keys [controller controller-content actions silent] :or { silent false } }]
    (if-let [controllers-directory (util/find-controllers-directory)]
      (do
        (when-let [controller-file (builder/create-controller-file 
                                { :controller controller, 
                                  :controllers-directory controllers-directory, 
                                  :silent silent })]
          (file-utils/write-file-content controller-file controller-content))
        (controller-test-generator/generate-functional-test controller actions silent))
      (logging/error "Could not find the controllers directory")))

(defn
#^{ :doc "Generates the controller content and saves it into the given controller file." }
  generate-file-content 
  [{ :keys [controller actions silent] :or { silent false } }]
    (create-controller-files
      { :controller controller
        :controller-content (generate-controller-content controller (generate-all-action-functions actions))
        :actions actions
        :silent silent }))

(defn
#^{ :doc "Creates the controller file associated with the given controller." }
  generate-controller-file
  [{ :keys [controller actions silent] :or { actions (), silent false } }]
    (if (and controller actions)
      (generate-file-content { :controller controller, :actions actions, :silent silent })
      (when (not silent)
        (controller-usage))))

(defn 
#^{ :doc "Generates a controller file for the controller name and actions in params." }
  generate [params]
  (generate-controller-file { :controller (first params), :actions (rest params) }))