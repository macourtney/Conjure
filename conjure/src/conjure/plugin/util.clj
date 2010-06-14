(ns conjure.plugin.util
  (:import [java.io File FileNotFoundException])
  (:require [clojure.contrib.logging :as logging]
            [clojure.contrib.seq-utils :as seq-utils]
            [clojure.contrib.str-utils :as str-utils]
            [clojure.contrib.test-is :as test-is]
            [conjure.config.environment :as environment]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.loading-utils :as loading-utils]))

(def plugins-dir "plugins")
(def plugin-file-name "plugin.clj")
(def test-directory-name "test")

(defn 
#^{ :doc "Finds the plugins directory." }
  find-plugins-directory []
  (environment/find-in-source-dir plugins-dir))

(defn
#^{ :doc "Returns the namespace string for the plugin.clj file for the plugin with the given name." }
  plugin-namespace-name [plugin-name]
  (str "plugins." (loading-utils/underscores-to-dashes plugin-name) ".plugin"))

(defn
#^{ :doc "Returns namespace for the plugin with the given name. If the plugin namespace does not exist, this function 
returns nil." }
  plugin-ns [plugin-name]
  (let [plugin-namspace-symbol (symbol (plugin-namespace-name plugin-name))]
    (try
      (require plugin-namspace-symbol)
      (find-ns plugin-namspace-symbol)
      (catch FileNotFoundException e
        nil))))

(defn
#^{ :doc "Returns the plugin name from the given plugin namespace." }
  plugin-name-from-namespace [plugin-namespace]
  (when plugin-namespace
    (if (string? plugin-namespace)
      (second (str-utils/re-split #"\." plugin-namespace))
      (plugin-name-from-namespace (name (ns-name plugin-namespace))))))

(defn
  #^{ :doc "Returns the install function for the plugin with the given name or nil if the install function could not be
found." }
  install-fn [plugin-name]
  (let [plugin-namespace (plugin-ns plugin-name)]
    (when plugin-namespace
      (ns-resolve plugin-namespace 'install))))

(defn
  #^{ :doc "Returns the uninstall function for the plugin with the given name or nil if the uninstall function could not
be found." }
  uninstall-fn [plugin-name]
  (let [plugin-namespace (plugin-ns plugin-name)]
    (when plugin-namespace
      (ns-resolve plugin-namespace 'uninstall))))

(defn
  #^{ :doc "Returns the initialize function for the plugin with the given name." }
  initialize-fn [plugin-name]
  (let [plugin-namespace (plugin-ns plugin-name)]
    (when plugin-namespace
      (ns-resolve plugin-namespace 'initialize))))

(defn
#^{ :doc "Returns the directory for the given plugin." }
  plugin-directory [plugin-name]
  (File. (find-plugins-directory) (loading-utils/dashes-to-underscores plugin-name)))

(defn
  is-plugin-namespace? [namespace]
  (when namespace
    (if (string? namespace)
      (and (.startsWith namespace (str plugins-dir ".")) (.endsWith namespace ".plugin"))
      (is-plugin-namespace? (name (ns-name namespace))))))

(defn
#^{ :doc "Returns a sequence of all model namespaces." }
  all-plugin-namespaces []
  (filter is-plugin-namespace? (all-ns)))

(defn
#^{ :doc "Returns a list of all plugins in the app." }
  all-plugins []
  (map plugin-name-from-namespace (all-plugin-namespaces)))

(defn
#^{ :doc "Returns a sequence of all of the initialize functions for all plugins in the app." }
  all-initialize-fns []
  (map initialize-fn (all-plugins)))

(defn
#^{ :doc "Runs the initialize function for all plugins in the app." }
  initialize-all-plugins []
  (doseq [initialize-fn (all-initialize-fns)]
    (try
      (initialize-fn)
      (catch Throwable t
        (logging/error (str "An error occured when initializing plugin: " initialize-fn) t)))))

(defn
#^{ :doc "Returns the test directory for the given plugin. If the test directory does not exist, this function returns
nil." }
  plugin-test-directory [plugin-name]
  (let [test-dir (File. (plugin-directory plugin-name) test-directory-name)]
    (when (.exists test-dir)
      test-dir)))

(defn
#^{ :doc "Returns the namespace string for the test with the given name and the plugin with the given name." }
  test-namespace-name [plugin-name test-name]
  (str "plugins." (loading-utils/underscores-to-dashes plugin-name) ".test." 
    (loading-utils/underscores-to-dashes test-name)))

(defn
#^{ :doc "Returns all of the test clj files for the given plugin." }
  test-files [plugin-name]
  (let [test-dir (plugin-test-directory plugin-name)]
    (if test-dir
      (filter #(.isFile %1) (seq-utils/flatten (file-seq test-dir)))
      '())))

(defn
#^{ :doc "Loads the given test file." }
  load-test-file [test-file]
  (load-file (. test-file getPath)))

(defn
#^{ :doc "Returns the namespace for the given plugin clj file." }
  plugin-file-namespace [plugin-file]
  (let [app-path (. (find-plugins-directory) getPath)
        file-parent-path (. plugin-file getParent)]
    (symbol 
      (str "plugins." (loading-utils/namespace-string-for-file
        (. file-parent-path substring (. app-path length)) (. plugin-file getName))))))

(defn
#^{ :doc "Runs all the tests in the given tests to run. Tests to run must be a sequence of namespace strings." }
  run-test-list [tests-to-run]
  (doseq [test-namespace-str tests-to-run]
    (let [clj-file (File. (.getParentFile (find-plugins-directory)) (loading-utils/symbol-string-to-clj-file test-namespace-str))]
      (logging/info (str "clj-file: " (.getPath clj-file)))
      (load-file (.getPath clj-file))))
  (apply test-is/run-tests (map symbol tests-to-run)))

(defn
#^{ :doc "Runs all the tests in the given plugin." }
  run-all-plugin-tests [plugin-name]
  (let [all-test-files (test-files plugin-name)]
    (doseq [test-file all-test-files]
      (load-test-file test-file))
    (apply test-is/run-tests (map plugin-file-namespace all-test-files))))

(defn
#^{ :doc "Runs all of the tests in the given plugin." }
  run-plugin-tests [plugin-name tests-to-run]
  (if (and tests-to-run (not-empty tests-to-run))
    (run-test-list tests-to-run)
    (run-all-plugin-tests plugin-name)))