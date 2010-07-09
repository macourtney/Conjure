(ns conjure.core.plugin.util
  (:import [java.io File FileNotFoundException])
  (:require [clojure.contrib.logging :as logging]
            [clojure.contrib.seq-utils :as seq-utils]
            [clojure.contrib.str-utils :as str-utils]
            [clojure.contrib.test-is :as test-is]
            [conjure.core.config.environment :as environment]
            [conjure.core.util.file-utils :as file-utils]
            [conjure.core.util.loading-utils :as loading-utils]))

(def plugins-dir "plugins")
(def plugin-file-name "plugin.clj")

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
  (let [plugins-directory (find-plugins-directory)]
    (when plugins-directory
      (File. plugins-directory (loading-utils/dashes-to-underscores plugin-name)))))

(defn
  is-plugin-namespace? [namespace]
  (when namespace
    (cond
      (string? namespace)
        (and (.startsWith namespace (str plugins-dir ".")) (.endsWith namespace ".plugin"))
      (symbol? namespace)
        (is-plugin-namespace? (str namespace))
      true 
        (is-plugin-namespace? (name (ns-name namespace))))))

(defn
#^{ :doc "Returns a sequence of all model namespaces." }
  all-plugin-namespaces []
  (filter identity (map plugin-ns (loading-utils/all-class-path-file-names plugins-dir))))

(defn
#^{ :doc "Returns a list of all plugins in the app." }
  all-plugins []
  (map plugin-name-from-namespace (all-plugin-namespaces)))

(defn
#^{ :doc "Returns a sequence of all of the initialize functions for all plugins in the app." }
  all-initialize-fns []
  (filter identity (map initialize-fn (all-plugins))))

(defn
#^{ :doc "Runs the initialize function for all plugins in the app." }
  initialize-all-plugins []
  (doseq [plugin-name (all-plugins)]
    (logging/info (str "Initializing plugin: " plugin-name))
    (if-let [init-fn (initialize-fn plugin-name)]
      (try
        (init-fn)
        (catch Throwable t
          (logging/error (str "An error occured when initializing plugin: " initialize-fn) t)))
      (logging/error (str "Plugin, " plugin-name ", does not have an initialize function.")))))

(defn
#^{ :doc "Returns the namespace for the given plugin clj file." }
  plugin-file-namespace [plugin-file]
  (let [app-path (.getPath (find-plugins-directory))
        file-parent-path (.getParent plugin-file)]
    (symbol 
      (str "plugins." (loading-utils/namespace-string-for-file
        (.substring file-parent-path (.length app-path)) (.getName plugin-file))))))

(defn
#^{ :doc "Returns the namespace string for the test with the given name and the plugin with the given name." }
  test-namespace-name [plugin-name]
  (str "plugins." (loading-utils/underscores-to-dashes plugin-name) ".test-plugin"))