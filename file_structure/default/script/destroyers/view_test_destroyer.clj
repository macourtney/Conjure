(ns destroyers.view-test-destroyer
  (:require [conjure.test.util :as util]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.loading-utils :as loading-utils]))

(defn
#^{:doc "Prints out how to use the destroy view test command."}
  usage []
  (println "You must supply a controller and action (Like hello-world show).")
  (println "Usage: ./run.sh script/destroy.clj view-test <controller> <action>"))

(defn
#^{:doc "Destroys the controller file from the given controller."}
  destroy-view-test-file 
  ([controller action])
  ([controller action silent]
    (if (and controller action)
      (let [controller-view-unit-test-dir (util/find-controller-view-unit-test-directory controller)]
        (if controller-view-unit-test-dir
          (let [view-unit-test-file (util/view-unit-test-file controller action controller-view-unit-test-dir)]
            (if view-unit-test-file
              (let [is-deleted (. view-unit-test-file delete)] 
                (if (not silent) (println "File" (. view-unit-test-file getName) (if is-deleted "destroyed." "not destroyed.")))
                (let [controller-dir (. view-unit-test-file getParentFile)]
                  (file-utils/delete-all-if-empty controller-dir (util/find-view-unit-test-directory) (util/find-unit-test-directory))))
              (if (not silent) (println "View test file not found. Doing nothing."))))
          (if (not silent) 
            (do
              (println "Could not find the " (loading-utils/dashes-to-underscores action) " test directory.")
              (println "Command ignored.")))))
      (usage))))

(defn
#^{:doc "Destroys a controller test file for the controller name given in params."}
  destroy-view-test [params]
  (destroy-view-test-file (first params) (second params)))

(defn
#^{:doc "Destroys all of the files created by the view_test_generator."}
  destroy-all-dependencies 
  ([controller action] (destroy-all-dependencies controller action false))
  ([controller action silent]
    (destroy-view-test-file controller action silent)))