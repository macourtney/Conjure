(ns destroyers.controller-test-destroyer
  (:require [conjure.test.util :as util]
            [conjure.util.file-utils :as file-utils]))

(defn
#^{:doc "Prints out how to use the destroy controller test command."}
  controller-usage []
  (println "You must supply a controller (Like hello-world).")
  (println "Usage: ./run.sh script/destroy.clj controller-test <controller>"))

(defn
#^{:doc "Destroys the controller file from the given controller."}
  destroy-controller-test-file [controller silent]
  (if controller
    (let [functional-test-directory (util/find-functional-test-directory)]
      (if functional-test-directory
        (let [functional-test-file (util/functional-test-file controller functional-test-directory)]
          (if functional-test-file
            (let [is-deleted (. functional-test-file delete)] 
              (if (not silent) 
                (println "File" (. functional-test-file getName) (if is-deleted "destroyed." "not destroyed.")))
              (if is-deleted (file-utils/delete-if-empty functional-test-directory)))
            (if (not silent) (println "Controller test file not found. Doing nothing."))))
        (if (not silent) 
            (do
            (println "Could not find the functional test directory.")
            (println "Command ignored.")))))
    (if (not silent) (controller-usage))))

(defn
#^{:doc "Destroys a controller test file for the controller name given in params."}
  destroy-controller-test [params]
  (destroy-controller-test-file (first params) false))

(defn
#^{:doc "Destroys all of the files created by the controller_test_generator."}
  destroy-all-dependencies 
  ([controller silent]
    (destroy-controller-test-file controller silent)))