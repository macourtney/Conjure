(ns conjure.script.generators.xml-view-generator
  (:import [java.io File])
  (:require [clojure.tools.logging :as logging]
            [conjure.view.builder :as builder]
            [conjure.view.util :as util]
            [clojure.tools.file-utils :as file-utils]
            [clojure.tools.loading-utils :as loading-utils]
            [conjure.script.generators.view-test-generator :as view-test-generator]
            [conjure.script.view.util :as script-view-util]))
  
(defn
#^{:doc "Returns xml view content which sets up the standard xml view namespace and defines an xml view. The given 
inner-content is added to the body of the xml view code."}
  generate-standard-content [xml-namespace inner-content]
  (str "(ns " xml-namespace "
  (:use conjure.core.view.xml-base)
  (:require [clojure.contrib.prxml :as prxml]))

(def-xml []
  (prxml/prxml 
    " inner-content "))"))
  
(defn
#^{:doc "Generates the xml view content and saves it into the given xml view file."}
  generate-file-content
  ([xml-view-file service] (generate-file-content xml-view-file service nil))
  ([xml-view-file service xml-content]
    (let [xml-view-namespace (util/view-namespace xml-view-file)
          xml-view-content (str (if xml-content xml-content (generate-standard-content xml-view-namespace (str "[:p \"You can change this text in app/views/" (loading-utils/dashes-to-underscores service) "/" (. xml-view-file getName) "\"]"))))]
      (file-utils/write-file-content xml-view-file xml-view-content))))

(defn
#^{:doc "Creates the view file associated with the given service and action."}
  generate-xml-view-file
    ([service action silent]
      (if (and service action)
        (when-let [view-directory (script-view-util/find-views-directory)]
          (do 
            (let [params { :views-directory view-directory, 
                           :service service,
                           :action action,
                           :silent silent }
                  service-directory (builder/find-or-create-service-directory params)]
              (when-let [view-file (builder/create-view-file (assoc params :service-directory service-directory))]
                (generate-file-content view-file service nil)))
            (view-test-generator/generate-unit-test service action silent)))
        (script-view-util/generate-usage "xml-view"))))

(defn 
#^{:doc "Generates a migration file for the migration name given in params."}
  generate [params]
  (generate-xml-view-file (first params) (second params) false))