(ns conjure.script.generators.plugin-generator
  (:import [java.io File])
  (:require [clojure.contrib.logging :as logging]
            [conjure.core.config.environment :as environment]
            [conjure.core.plugin.builder :as plugin-builder]
            [conjure.core.plugin.util :as plugin-util]
            [clojure.tools.file-utils :as file-utils]
            [clojure.tools.loading-utils :as loading-utils]))

(defn
#^{ :doc "Prints out how to use the generate plugin command." }
  usage []
  (println "You must supply a plugin name (Like hello-world).")
  (println "Usage: ./run.sh script/generate.clj plugin <name>"))
  
(defn
#^{ :doc "Generates the install function." }
  generate-install-function []
  (str "(defn install [arguments])"))

(defn
#^{ :doc "Generates the uninstall function." }
  generate-uninstall-function []
  (str "(defn uninstall [arguments])"))

(defn
#^{ :doc "Generates the initialize function." }
  generate-initialize-function []
  (str "(defn initialize [])"))

(defn
#^{ :doc "Generates the content of the plugin.clj file." }
  generate-plugin-content
  ([plugin-name] 
    (generate-plugin-content
      plugin-name
      (generate-install-function)
      (generate-uninstall-function)
      (generate-initialize-function)))
  ([plugin-name install-function uninstall-function initialize-function] 
    (str "(ns " (plugin-util/plugin-namespace-name plugin-name) "
  (:use conjure.core.plugin.base))

" install-function "

" uninstall-function "

" initialize-function)))

(defn
#^{ :doc "Returns the file of the generic plugin test." }
  test-file [plugin-name]
  (File. (environment/find-test-dir) (str (loading-utils/dashes-to-underscores plugin-name) "/test_plugin.clj")))

(defn
#^{ :doc "Generates the content of the test file." }
  generate-test-content [plugin-name]
  (str "(ns " (plugin-util/test-namespace-name plugin-name) "
  (use clojure.contrib.test-is
      " (plugin-util/plugin-namespace-name plugin-name) "))

(deftest test-initialize
  (initialize))"))

(defn
#^{ :doc "Creates a plugin directory with a default plugin.clj file." }
  create-plugin-files 
  [{ :keys [name content silent] :or { silent false } }]
    (let [plugins-directory (plugin-util/find-plugins-directory)]
      (if plugins-directory
        (do
          (let [plugin-file (plugin-builder/create-plugin-files { :name name, :silent silent })]
            (when plugin-file
              (file-utils/write-file-content plugin-file (or content (generate-plugin-content name)))
              (file-utils/write-file-content (test-file name) (generate-test-content name)))))
        (logging/error (str "Could not find plugins directory: " plugins-directory)))))

(defn
#^{ :doc "Generates the binding content and saves it into the given binding file." }
  generate-file 
  [{ :keys [name silent] :or { silent false } }]
    (create-plugin-files
      { :name name, 
        :content (generate-plugin-content name), 
        :silent silent }))

(defn
#^{ :doc "Creates the binding file associated with the given controller and action." }
  generate-plugin-files
  [{ :keys [name silent] :or { silent false } }]
    (if name
      (generate-file { :name name, :silent silent })
      (if (not silent) (usage))))
        
(defn 
#^{ :doc "Generates a binding file for the controller name and action in params." }
  generate [params]
  (generate-plugin-files { :name (first params) }))