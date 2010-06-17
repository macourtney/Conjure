(ns conjure.core.plugin.builder
  (:import [java.io File])
  (:require [clojure.contrib.logging :as logging]
            [conjure.core.plugin.util :as plugin-util]
            [conjure.core.util.loading-utils :as loading-utils]))

(defn
#^{ :doc "Finds or creates a the plugin directory for the given plugin name." }
  find-or-create-plugin-directory 
  [plugin-name]
  (when plugin-name
    (let [plugin-dir (plugin-util/plugin-directory plugin-name)]
      (when (not (and plugin-dir (.exists plugin-dir)))
        (logging/info (str "Creating plugin directory " (.getName plugin-dir) "..."))
        (.mkdirs plugin-dir))
      plugin-dir)))

(defn
#^{ :doc "Creates a subdirectory wit the given name in the given plugin directory." }
  create-plugin-subdirectory
  [plugin-dir subdirectory-name]
  (when plugin-dir
    (let [lib-dir (File. plugin-dir subdirectory-name)]
      (when (not (.exists lib-dir))
        (logging/info (str "Creating plugin " subdirectory-name " directory..."))
        (.mkdir lib-dir)))))

(defn
#^{ :doc "Creates a test directory in the given plugin directory." }
  create-test-directory
  [plugin-dir]
  (create-plugin-subdirectory plugin-dir plugin-util/test-directory-name))

(defn
#^{ :doc "Creates all of the plugin files and directories and returns the newly created plugin.clj file." }
  create-plugin-files
  [ { :keys [name silent] 
      :or { silent false } }]
    (if name
      (let [plugin-dir (find-or-create-plugin-directory name)
            plugin-file (File. (plugin-util/plugin-directory name) plugin-util/plugin-file-name)]
        (if (. plugin-file exists)
          (logging/info (str (. plugin-file getName) " already exists. Doing nothing."))
          (do
            (logging/info (str "Creating plugin file " (.getName plugin-file) "..."))
            (.createNewFile plugin-file)
            (create-test-directory plugin-dir)
            plugin-file)))))