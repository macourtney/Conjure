(ns generators.view-generator
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
  (println "Usage: ./run.sh script/generate.clj view <controller> <action>"))
  
(defn
#^{ :doc "Returns view content which sets up the standard view namespace and defines a view. The given inner-content is 
added to the body of the view code." }
  generate-standard-content
  ([view-namespace inner-content] (generate-standard-content view-namespace inner-content ""))
  ([view-namespace inner-content view-params] (generate-standard-content view-namespace inner-content view-params nil))
  ([view-namespace inner-content view-params requires]
    (str "(ns " view-namespace "
  (:use conjure.view.base)
  (:require [clj-html.core :as html]" (if requires (str "\n" requires)) "))

(defview [" view-params "]
  (html/html 
    " inner-content "))")))

(defn
#^{ :doc "Returns the content of a view with standard namespace and imports with the given view parameters and inner 
content." }
  generate-view-content [controller action inner-content view-params requires]
  (generate-standard-content (util/view-namespace-by-action controller action) inner-content view-params requires))

(defn
#^{ :doc "Generates the view content and saves it into the given view file." }
  generate-file-content
    ([view-file controller] (generate-file-content view-file controller nil))
    ([view-file controller content]
      (let [view-namespace (util/view-namespace controller view-file)
            view-content (str (if content 
                                content 
                                (generate-standard-content 
                                  view-namespace 
                                  (str 
                                    "[:p \"You can change this text in app/views/" 
                                    (loading-utils/dashes-to-underscores controller) 
                                    "/" 
                                    (. view-file getName) 
                                    "\"]"))))]
        (file-utils/write-file-content view-file view-content))))

(defn
#^{:doc "Creates the view file associated with the given controller and action."}
  generate-view-file
    ([{ :keys [controller action content silent test-content] 
        :or { content nil, silent false, test-content nil } }]
      (if (and controller action)
        (let [view-directory (util/find-views-directory)]
          (if view-directory
            (do 
              (let [params { :views-directory view-directory, 
                             :controller controller,
                             :action action,
                             :silent silent }
                    controller-directory (builder/find-or-create-controller-directory params)
                    view-file (builder/create-view-file 
                                (assoc params :controller-directory controller-directory))]
                (if view-file
                  (generate-file-content view-file controller content)))
              (view-test-generator/generate-unit-test controller action silent test-content))
            (if (not silent)
              (do
                (println "Could not find views directory.")
                (println view-directory)))))
        (view-usage))))
        
(defn 
#^{:doc "Generates a migration file for the migration name given in params."}
  generate-view [params]
  (generate-view-file { :controller (first params), :action (second params) }))