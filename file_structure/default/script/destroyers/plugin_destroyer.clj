(ns destroyers.binding-destroyer
  (:require [clojure.contrib.logging :as logging]
            [conjure.plugin.util :as plugin-util]
            [destroyers.binding-test-destroyer :as binding-test-destroyer]
            [destroyers.view-destroyer :as view-destroyer]))

(defn
#^{:doc "Prints out how to use the destroy plugin command."}
  usage []
  (println "You must supply a name (Like hello-world).")
  (println "Usage: ./run.sh script/destroy.clj plugin <name>"))

(defn
#^{:doc "Destroys the binding file for the given controller and action."}
  destroy-plugin [plugin-name]
  (if plugin-name
    (let [plugin-directory (plugin-util/plugin-directory)]
      (if (and plugin-directory (.exists plugin-directory))
        (let [is-deleted (.delete plugin-directory)] 
          (logging/info (str "Plugin " (.getName plugin-directory) (if is-deleted " destroyed." " not destroyed."))))
        (do
          (logging/error (str "Could not find plugin directory.: " plugin-directory))
          (logging/error "Command ignored."))))
    (if (not silent) (usage))))

(defn
#^{:doc "Destroys a controller file for the controller and action given in params."}
  destroy [params]
  (destroy-plugin (first params)))