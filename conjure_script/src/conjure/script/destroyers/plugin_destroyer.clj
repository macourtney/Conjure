(ns conjure.script.destroyers.plugin-destroyer
  (:require [clojure.tools.logging :as logging]
            [conjure.core.plugin.util :as plugin-util]
            [clojure.tools.file-utils :as file-utils]))

(defn
#^{:doc "Prints out how to use the destroy plugin command."}
  usage []
  (println "You must supply a name (Like hello-world).")
  (println "Usage: ./run.sh script/destroy.clj plugin <name>"))

(defn
#^{:doc "Destroys the plugin directory for the given plugin name."}
  destroy-plugin [plugin-name silent]
  (if plugin-name
    (let [plugin-directory (plugin-util/plugin-directory plugin-name)]
      (if (and plugin-directory (.exists plugin-directory))
        (let [is-deleted (file-utils/recursive-delete plugin-directory)]
          (logging/info (str "Plugin " (.getName plugin-directory) (if is-deleted " destroyed." " not destroyed."))))
        (do
          (logging/error (str "Could not find plugin directory.: " plugin-directory))
          (logging/error "Command ignored."))))
    (if (not silent) (usage))))

(defn
#^{:doc "Destroys a plugin directory for the given plugin name."}
  destroy [params]
  (destroy-plugin (first params) false))