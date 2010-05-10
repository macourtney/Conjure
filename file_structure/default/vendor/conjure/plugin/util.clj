(ns conjure.plugin.util
  (:import [java.io File FileNotFoundException])
  (:require [clojure.contrib.seq-utils :as seq-utils]
            [conjure.util.loading-utils :as loading-utils]))

(def plugins-dir "plugins")
(def plugin-file-name "plugin.clj")
(def lib-directory-name "lib")
(def test-directory-name "test")

(defn 
#^{ :doc "Finds the plugins directory." }
  find-plugins-directory []
  (seq-utils/find-first (fn [directory] (.. directory (getPath) (endsWith plugins-dir)))
    (.listFiles (loading-utils/get-classpath-dir-ending-with "vendor"))))

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
#^{ :doc "Returns the directory for the given plugin." }
  plugin-directory [plugin-name]
  (File. (find-plugins-directory) (loading-utils/dashes-to-underscores plugin-name)))