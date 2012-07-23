(ns conjure.script.destroyers.view-test-destroyer
  (:require [clojure.tools.logging :as logging]
            [clojure.tools.file-utils :as file-utils]
            [clojure.tools.loading-utils :as loading-utils]
            [conjure.script.view.util :as script-view-util]
            [conjure.test.util :as util]))

(defn
#^{:doc "Destroys the view test file from the given service."}
  destroy-view-test-file 
  ([service action] (destroy-view-test-file service action false))
  ([service action silent]
    (if (and service action)
      (when-let [view-unit-test-file (script-view-util/find-view-unit-test-file service action)]
        (let [is-deleted (.delete view-unit-test-file)] 
          (logging/info (str "File " (.getName view-unit-test-file) (if is-deleted " destroyed." " not destroyed.")))
          (let [service-dir (.getParentFile view-unit-test-file)]
            (file-utils/delete-all-if-empty service-dir (util/find-view-unit-test-directory) (util/find-unit-test-directory)))))
      (script-view-util/destroy-usage "view-test"))))

(defn
#^{:doc "Destroys a view test file for the service name given in params."}
  destroy [params]
  (destroy-view-test-file (first params) (second params)))

(defn
#^{:doc "Destroys all of the files created by the view_test_generator."}
  destroy-all-dependencies 
  ([service action] (destroy-all-dependencies service action false))
  ([service action silent]
    (destroy-view-test-file service action silent)))