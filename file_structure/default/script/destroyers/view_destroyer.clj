(ns destroyers.view-destroyer
  (:require [conjure.view.util :as util]))

(defn
#^{:doc "Prints out how to use the destroy view command."}
  view-usage []
  (println "You must supply a controller and action name (Like hello-world).")
  (println "Usage: ./run.sh script/destroy.clj view <controller> <action>"))

(defn
#^{:doc "Destroys the view file from the given controller and action."}
  destroy-view-file [controller action]
  (if (and controller action)
    (let [view-directory (util/find-views-directory)]
      (if view-directory
        (let [controller-directory (util/find-controller-directory view-directory controller)]
          (if controller-directory
            (let [view-file (util/find-view-file controller-directory action)]
              (if view-file
                (let [is-deleted (. view-file delete)] 
                  (println "File" (. view-file getName) (if is-deleted "destroyed." "not destroyed.")))
                (println "View file not found. Doing nothing.")))
            (println "The directory for controller" controller "was not found. Doing nothing.")))
        (do
          (println "Could not find views directory.")
          (println view-directory)
          (println "Command ignored."))))
    (view-usage)))

(defn
#^{:doc "Destroys a view file for the view name given in params."}
  destroy-view [params]
  (destroy-view-file (first params) (second params)))