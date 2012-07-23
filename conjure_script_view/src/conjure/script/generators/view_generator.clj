(ns conjure.script.generators.view-generator
  (:import [java.io File])
  (:require [clojure.tools.logging :as logging]
            [conjure.view.builder :as builder]
            [conjure.view.util :as util]
            [clojure.tools.file-utils :as file-utils]
            [clojure.tools.loading-utils :as loading-utils]
            [conjure.script.generators.view-test-generator :as view-test-generator]
            [conjure.script.view.util :as script-view-util]))
  
(defn
#^{ :doc "Returns view content which sets up the standard view namespace and defines a view. The given inner-content is 
added to the body of the view code." }
  generate-standard-content
  ([view-namespace inner-content] (generate-standard-content view-namespace inner-content ""))
  ([view-namespace inner-content view-params] (generate-standard-content view-namespace inner-content view-params nil))
  ([view-namespace inner-content view-params requires]
    (str "(ns " view-namespace "
  (:use conjure.core.view.base)
  " (when requires (str "(:require " requires ")")) ")

(def-view [" view-params "]
  " inner-content ")")))

(defn
#^{ :doc "Returns the content of a view with standard namespace and imports with the given view parameters and inner 
content." }
  generate-view-content [service action inner-content view-params requires]
  (generate-standard-content (util/view-namespace-by-action service action) inner-content view-params requires))

(defn
#^{ :doc "Generates the view content and saves it into the given view file." }
  generate-file-content
    ([view-file service] (generate-file-content view-file service nil))
    ([view-file service content]
      (let [view-namespace (util/view-namespace view-file)
            view-content (str (if content 
                                content 
                                (generate-standard-content 
                                  view-namespace 
                                  (str 
                                    "[:p \"You can change this text in app/views/" 
                                    (loading-utils/dashes-to-underscores service) 
                                    "/" 
                                    (.getName view-file) 
                                    "\"]"))))]
        (file-utils/write-file-content view-file view-content))))

(defn
#^{:doc "Creates the view file associated with the given service and action."}
  generate-view-file
    ([{ :keys [service action content silent test-content] 
        :or { content nil, silent false, test-content nil } }]
      (if (and service action)
        (when-let [view-directory (script-view-util/find-views-directory)]
          (do 
              (let [params { :views-directory view-directory, 
                             :service service,
                             :action action,
                             :silent silent }
                    service-directory (builder/find-or-create-service-directory params)]
                (when-let [view-file (builder/create-view-file 
                                       (assoc params :service-directory service-directory))]
                  (generate-file-content view-file service content)))
              (view-test-generator/generate-unit-test service action silent test-content)))
        (script-view-util/generate-usage "view"))))
        
(defn 
#^{:doc "Generates a migration file for the migration name given in params."}
  generate [params]
  (generate-view-file { :service (first params), :action (second params) }))