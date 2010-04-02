(ns destroyers.controller-destroyer
  (:require [clojure.contrib.logging :as logging]
            [conjure.controller.util :as util]
            [destroyers.controller-test-destroyer :as controller-test-destroyer]
            [destroyers.binding-destroyer :as binding-destroyer]))

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
              (logging/info (str "File " (. controller-file getName) (if is-deleted " destroyed." " not destroyed."))))
            (logging/info "Controller file not found. Doing nothing.")))
        (do
          (logging/error (str "Could not find controllers directory.: " controllers-directory))
          (logging/error "Command ignored."))))
    (if (not silent) (controller-usage))))

(defn
#^{:doc "Destroys a controller file for the controller name given in params."}
  destroy [params]
  (destroy-controller-file (first params)))

(defn
#^{:doc "Destroys all of the files created by the controller_generator."}
  destroy-all-dependencies
  [{ :keys [controller actions silent] :or { actions (), silent false } }]
    (destroy-controller-file controller silent)
    (doseq [action actions]
      (binding-destroyer/destroy-all-dependencies controller action silent))
    (controller-test-destroyer/destroy-all-dependencies controller silent))