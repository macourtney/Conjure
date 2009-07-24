(ns destroyers.controller-destroyer
  (:require [conjure.controller.util :as util]
            [destroyers.controller-test-destroyer :as controller-test-destroyer]
            [destroyers.view-destroyer :as view-destroyer]))

(defn
#^{:doc "Prints out how to use the destroy controller command."}
  controller-usage []
  (println "You must supply a controller (Like hello-world).")
  (println "Usage: ./run.sh script/destroy.clj controller <controller>"))

(defn
#^{:doc "Destroys the controller file from the given controller."}
  destroy-controller-file [controller]
  (if controller
    (let [controllers-directory (util/find-controllers-directory)]
      (if controllers-directory
        (let [controller-file (util/find-controller-file controllers-directory controller)]
          (if controller-file
            (let [is-deleted (. controller-file delete)] 
              (println "File" (. controller-file getName) (if is-deleted "destroyed." "not destroyed.")))
            (println "Controller file not found. Doing nothing.")))
        (do
          (println "Could not find controllers directory.")
          (println controllers-directory)
          (println "Command ignored."))))
    (controller-usage)))

(defn
#^{:doc "Destroys a controller file for the controller name given in params."}
  destroy-controller [params]
  (destroy-controller-file (first params)))

(defn
#^{:doc "Destroys all of the files created by the controller_generator."}
  destroy-all-dependencies
  ([controller] (destroy-all-dependencies controller ())) 
  ([controller actions]
    (destroy-controller-file controller)
    (doall (map #(view-destroyer/destroy-view-file controller %) actions))
    (controller-test-destroyer/destroy-all-dependencies controller)))