(ns generators.controller-generator
  (:require [conjure.controller.builder :as builder]
            [conjure.controller.util :as util]
            [conjure.util.file-utils :as file-utils]
            [clojure.contrib.str-utils :as str-utils]
            [generators.view-generator :as view-generator]
            [generators.controller-test-generator :as controller-test-generator]))

(defn
#^{ :doc "Prints out how to use the generate controller command." }
  controller-usage []
  (println "You must supply a controller name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj controller <controller> [action]*"))
  
(defn
#^{ :doc "Generates the action function for the given action." }
  generate-action-function [action]
  (str "(defn " action " [request-map]
  (render-view request-map))"))
  
(defn
#^{ :doc "Generates the action functions block for a controller file." }
  generate-all-action-functions [actions]
  (str-utils/str-join "\n\n" (map generate-action-function actions)))
  
(defn
#^{ :doc "Generates the view file for the given action." }
  generate-view-file [controller action silent]
  (view-generator/generate-view-file { :controller controller, :action action, :silent silent }))
  
(defn
#^{ :doc "Generates the content of the given controller file." }
  generate-controller-content 
  ([controller controller-content] (generate-controller-content controller controller-content nil))
  ([controller controller-content requires]
    (str "(ns " (util/controller-namespace controller) "
  (:use [conjure.controller.base])" (if requires (str "\n  (:require " requires ")")) ")

" controller-content)))

(defn
#^{ :doc "Creates a controller file with the given File and file content." }
  create-controller-files 
  [{ :keys [controller controller-content actions silent] :or { silent false } }]
    (let [controllers-directory (util/find-controllers-directory)]
      (if controllers-directory
        (do
          (let [controller-file (builder/create-controller-file 
                                  { :controller controller, 
                                    :controllers-directory controllers-directory, 
                                    :silent silent })]
            (if controller-file
              (file-utils/write-file-content controller-file controller-content)))
          (controller-test-generator/generate-functional-test controller actions silent))
        (if (not silent) 
          (do
            (println "Could not find controllers directory.")
            (println controllers-directory))))))

(defn
#^{ :doc "Generates the controller content and saves it into the given controller file." }
  generate-file-content 
  [{ :keys [controller actions silent] :or { silent false } }]
    (do
      (create-controller-files
        { :controller controller, 
          :controller-content (generate-controller-content controller (generate-all-action-functions actions)), 
          :actions actions,
          :silent silent })
      (doall (map #(generate-view-file controller % silent) actions))))

(defn
#^{ :doc "Creates the controller file associated with the given controller." }
  generate-controller-file
  [{ :keys [controller actions silent] :or { actions (), silent false } }]
    (if (and controller actions)
      (generate-file-content { :controller controller, :actions actions, :silent silent })
      (if (not silent) (controller-usage))))
        
(defn 
#^{ :doc "Generates a controller file for the controller name and actions in params." }
  generate-controller [params]
  (generate-controller-file { :controller (first params), :actions (rest params) }))