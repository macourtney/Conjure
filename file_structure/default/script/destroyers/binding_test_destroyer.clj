(ns destroyers.binding-test-destroyer
  (:require [clojure.contrib.logging :as logging]
            [conjure.test.util :as util]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.loading-utils :as loading-utils]))

(defn
#^{:doc "Prints out how to use the destroy binding test command."}
  usage []
  (println "You must supply a controller (Like hello-world) and action (like show-me).")
  (println "Usage: ./run.sh script/destroy.clj binding-test <controller> <action>"))

(defn
#^{:doc "Destroys the binding test file from the given controller."}
  destroy-binding-test-file 
  ([controller action])
  ([controller action silent]
    (if (and controller action)
      (let [controller-binding-unit-test-dir (util/find-controller-binding-unit-test-directory controller)]
        (if controller-binding-unit-test-dir
          (let [binding-unit-test-file (util/binding-unit-test-file controller action controller-binding-unit-test-dir)]
            (if binding-unit-test-file
              (let [is-deleted (. binding-unit-test-file delete)] 
                (logging/info (str "File " (. binding-unit-test-file getName) (if is-deleted " destroyed." " not destroyed.")))
                (let [controller-dir (. binding-unit-test-file getParentFile)]
                  (file-utils/delete-all-if-empty controller-dir (util/find-binding-unit-test-directory) (util/find-unit-test-directory))))
              (logging/info "Binding test file not found. Doing nothing.")))
          (do
            (logging/error (str "Could not find the binding " (loading-utils/dashes-to-underscores controller) " test directory."))
            (logging/error "Command ignored."))))
      (usage))))

(defn
#^{:doc "Destroys a binding test file for the controller name given in params."}
  destroy [params]
  (destroy-binding-test-file (first params) false))

(defn
#^{:doc "Destroys all of the files created by the binding_test_generator."}
  destroy-all-dependencies 
  ([controller action silent]
    (destroy-binding-test-file controller action silent)))