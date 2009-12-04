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
  destroy-controller-file [controller silent]
  (if controller
    (let [controllers-directory (util/find-controllers-directory)]
      (if controllers-directory
        (let [controller-file (util/find-controller-file controllers-directory controller)]
          (if controller-file
            (let [is-deleted (. controller-file delete)] 
              (if (not silent) (println "File" (. controller-file getName) (if is-deleted "destroyed." "not destroyed."))))
            (if (not silent) (println "Controller file not found. Doing nothing."))))
        (if (not silent) 
          (do
            (println "Could not find controllers directory.")
            (println controllers-directory)
            (println "Command ignored.")))))
    (if (not silent) (controller-usage))))

(defn
#^{:doc "Destroys a controller file for the controller name given in params."}
  destroy-controller [params]
  (destroy-controller-file (first params)))

(defn
#^{:doc "Destroys all of the files created by the controller_generator."}
  destroy-all-dependencies
  [{ :keys [controller actions silent] :or { actions (), silent false } }]
    (destroy-controller-file controller silent)
    (doall (map #(view-destroyer/destroy-all-dependencies controller % silent) actions))
    (controller-test-destroyer/destroy-all-dependencies controller silent))