(ns destroyers.binding-destroyer
  (:require [clojure.contrib.logging :as logging]
            [conjure.binding.util :as binding-util]
            [destroyers.binding-test-destroyer :as binding-test-destroyer]
            [destroyers.view-destroyer :as view-destroyer]))

(defn
#^{:doc "Prints out how to use the destroy binding command."}
  usage []
  (println "You must supply a controller (Like hello-world) and an action (like show-me).")
  (println "Usage: ./run.sh script/destroy.clj binding <controller> <action>"))

(defn
#^{:doc "Destroys the binding file for the given controller and action."}
  destroy-binding-file [controller action silent]
  (if controller
    (let [bindings-directory (binding-util/find-bindings-directory)]
      (if bindings-directory
        (let [binding-file (binding-util/find-binding-file bindings-directory controller action)]
          (if binding-file
            (let [is-deleted (. binding-file delete)] 
              (logging/info (str "File " (. binding-file getName) (if is-deleted " destroyed." " not destroyed."))))
            (logging/info "Binding file not found. Doing nothing.")))
        (do
          (logging/error (str "Could not find bindings directory.: " bindings-directory))
          (logging/error "Command ignored."))))
    (if (not silent) (usage))))

(defn
#^{:doc "Destroys a controller file for the controller and action given in params."}
  destroy [params]
  (destroy-binding-file (first params) (second params)))

(defn
#^{:doc "Destroys all of the files created by the binding_generator."}
  destroy-all-dependencies 
  ([controller action] (destroy-all-dependencies controller action false))
  ([controller action silent]
    (destroy-binding-file controller action silent)
    (view-destroyer/destroy-all-dependencies controller action silent)
    (binding-test-destroyer/destroy-all-dependencies controller action silent)))