(ns generators.binding-generator
  (:require [clojure.contrib.logging :as logging]
            [conjure.binding.builder :as bind-builder]
            [conjure.binding.util :as bind-util]
            [conjure.util.file-utils :as file-utils]
            [generators.view-generator :as view-generator]
            [generators.binding-test-generator :as binding-test-generator]))

(defn
#^{ :doc "Prints out how to use the generate binding command." }
  usage []
  (println "You must supply a controller name (Like hello-world) and an action name (like show-me).")
  (println "Usage: ./run.sh script/generate.clj binding <controller> <action>"))
  
(defn
#^{ :doc "Generates the action function for the given action." }
  generate-binding-function []
  (str "(defbinding [request-map]
  (render-view request-map))"))

(defn
#^{ :doc "Generates the view file for the given action." }
  generate-view-file [controller action silent]
  (view-generator/generate-view-file { :controller controller, :action action, :silent silent }))
  
(defn
#^{ :doc "Generates the content of the given binding file." }
  generate-binding-content 
  ([controller action binding-content] (generate-binding-content controller action binding-content nil))
  ([controller action binding-content requires]
    (str "(ns " (bind-util/binding-namespace controller action) "
  (:use conjure.binding.base)" (if requires (str "\n  (:require " requires ")")) ")

" binding-content)))

(defn
#^{ :doc "Creates a binding file with the given File and file content." }
  create-binding-files 
  [{ :keys [controller binding-content action silent] :or { silent false } }]
    (let [bindings-directory (bind-util/find-bindings-directory)]
      (if bindings-directory
        (do
          (let [controller-directory (bind-builder/find-or-create-controller-directory 
                                        { :bindings-directory bindings-directory,
                                          :controller controller,
                                          :silent silent })
                binding-file (bind-builder/create-binding-file 
                                  { :controller controller, 
                                    :action action,
                                    :controllers-directory controller-directory, 
                                    :silent silent })]
            (if binding-file
              (file-utils/write-file-content binding-file binding-content)))
          (binding-test-generator/generate-unit-test controller action silent)) 
        (logging/error (str "Could not find bindings directory: " bindings-directory)))))

(defn
#^{ :doc "Generates the binding content and saves it into the given binding file." }
  generate-file-content 
  [{ :keys [controller action silent] :or { silent false } }]
    (create-binding-files
      { :controller controller, 
        :binding-content (generate-binding-content controller action (generate-binding-function)), 
        :action action,
        :silent silent })
    (generate-view-file controller action silent))

(defn
#^{ :doc "Creates the binding file associated with the given controller and action." }
  generate-binding-file
  [{ :keys [controller action silent] :or { silent false } }]
    (if (and controller action)
      (generate-file-content { :controller controller, :action action, :silent silent })
      (if (not silent) (usage))))
        
(defn 
#^{ :doc "Generates a binding file for the controller name and action in params." }
  generate [params]
  (generate-binding-file { :controller (first params), :action (second params) }))