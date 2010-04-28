(ns conjure.plugin.util
  (:import [java.io File])
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
#^{ :doc "Returns the directory for the given plugin." }
  plugin-directory [plugin-name]
  (File. (find-plugins-directory) (loading-utils/dashes-to-underscores plugin-name)))