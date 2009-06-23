(ns generators.controller-generator
  (:require [conjure.controller.controller :as controller]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.loading-utils :as loading-utils]
            [clojure.contrib.str-utils :as str-utils]
            [generators.view-generator :as view-generator]))

(defn
#^{:doc "Prints out how to use the generate controller command."}
  controller-usage []
  (println "You must supply a controller name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj controller <controller> [action]*"))
  
(defn
#^{:doc "Generates the action function for the given action."}
  generate-action-function [action]
  (str "(defn " action " [request-map]
  (render-view request-map))"))
  
(defn
#^{:doc "Generates the action functions block for a controller file."}
  generate-all-action-functions [actions]
  (str-utils/str-join "\n\n" (map generate-action-function actions)))
  
(defn
#^{:doc "Generates the view file for the given action."}
  generate-view-file [controller action]
  (view-generator/generate-view-file controller action))
  
(defn
#^{:doc "Generates the controller content and saves it into the given controller file."}
  generate-file-content [controller-file actions]
      (let [controller (controller/controler-from-file controller-file)
            controller-namespace (controller/controller-namespace controller)
            content (str "(ns " controller-namespace "
  (:use [conjure.controller.controller]))

" (generate-all-action-functions actions))]
        (file-utils/write-file-content controller-file content)
        (doall (map (fn [action] (generate-view-file controller action)) actions))))

(defn
#^{:doc "Creates the controller file associated with the given controller."}
  generate-controller-file
    ([controller actions]
      (if (and controller actions)
        (let [controllers-directory (controller/find-controllers-directory)]
          (if controllers-directory
            (let [controller-file (controller/create-controller-file controllers-directory controller)]
                (if controller-file
                  (generate-file-content controller-file actions)))
            (do
              (println "Could not find controllers directory.")
              (println controllers-directory))))
        (controller-usage))))
        
(defn 
#^{:doc "Generates a controller file for the controller name and actions in params."}
  generate-controller [params]
  (generate-controller-file (first params) (rest params)))