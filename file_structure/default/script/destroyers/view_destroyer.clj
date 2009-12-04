(ns destroyers.view-destroyer
  (:require [conjure.view.util :as util]
            [destroyers.view-test-destroyer :as view-test-destroyer]))

(defn
#^{:doc "Prints out how to use the destroy view command."}
  view-usage []
  (println "You must supply a controller and action name (Like hello-world).")
  (println "Usage: ./run.sh script/destroy.clj view <controller> <action>"))

(defn
#^{:doc "Destroys the view file from the given controller and action."}
  destroy-view-file 
  ([controller action] (destroy-view-file controller action false))
  ([controller action silent]
    (if (and controller action)
      (let [view-directory (util/find-views-directory)]
        (if view-directory
          (let [controller-directory (util/find-controller-directory view-directory controller)]
            (if controller-directory
              (let [view-file (util/find-view-file controller-directory action)]
                (if view-file
                  (let [is-deleted (. view-file delete)] 
                    (if (not silent) (println "File" (. view-file getName) (if is-deleted "destroyed." "not destroyed."))))
                  (if (not silent) (println "View file not found. Doing nothing."))))
              (if (not silent) (println "The directory for controller" controller "was not found. Doing nothing."))))
          (if (not silent) 
            (do
              (println "Could not find views directory.")
              (println view-directory)
              (println "Command ignored.")))))
      (view-usage))))

(defn
#^{:doc "Destroys a view file for the view name given in params."}
  destroy-view [params]
  (destroy-view-file (first params) (second params)))

(defn
#^{:doc "Destroys all of the files created by the view_generator."}
  destroy-all-dependencies
  ([controller action] (destroy-all-dependencies controller action false))
  ([controller action silent]
    (destroy-view-file controller action silent)
    (view-test-destroyer/destroy-all-dependencies controller action silent)))