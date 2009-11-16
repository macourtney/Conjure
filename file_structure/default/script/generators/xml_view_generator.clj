(ns generators.xml-view-generator
  (:import [java.io File])
  (:require [conjure.view.builder :as builder]
            [conjure.view.util :as util]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.loading-utils :as loading-utils]
            [generators.view-test-generator :as view-test-generator]))

(defn
#^{:doc "Prints out how to use the generate view command."}
  view-usage []
  (println "You must supply a controller and action name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj xml-view <controller> <action>"))
  
(defn
#^{:doc "Returns xml view content which sets up the standard xml view namespace and defines an xml view. The given 
inner-content is added to the body of the xml view code."}
  generate-standard-content [xml-namespace inner-content]
  (str "(ns " xml-namespace "
  (:use conjure.view.xml-base)
  (:require [clojure.contrib.prxml :as prxml]))

(defxml []
  (prxml/prxml 
    " inner-content "))"))
  
(defn
#^{:doc "Generates the xml view content and saves it into the given xml view file."}
  generate-file-content
    ([xml-view-file controller] (generate-file-content xml-view-file controller nil))
    ([xml-view-file controller xml-content]
      (let [xml-view-namespace (util/view-namespace controller xml-view-file)
            xml-view-content (str (if xml-content xml-content (generate-standard-content xml-view-namespace (str "[:p \"You can change this text in app/views/" (loading-utils/dashes-to-underscores controller) "/" (. xml-view-file getName) "\"]"))))]
        (file-utils/write-file-content xml-view-file xml-view-content))))

(defn
#^{:doc "Creates the view file associated with the given controller and action."}
  generate-xml-view-file
    ([controller action] (generate-xml-view-file controller action nil))
    ([controller action xml-content]
      (if (and controller action)
        (let [view-directory (util/find-views-directory)]
          (if view-directory
            (do 
              (let [controller-directory (builder/find-or-create-controller-directory view-directory controller)
                    view-file (builder/create-view-file controller-directory action)]
                (if view-file
                  (generate-file-content view-file controller xml-content)))
              (view-test-generator/generate-unit-test controller action))
            (do
              (println "Could not find views directory.")
              (println view-directory))))
        (view-usage))))
        
(defn 
#^{:doc "Generates a migration file for the migration name given in params."}
  generate-view [params]
  (generate-xml-view-file (first params) (second params)))