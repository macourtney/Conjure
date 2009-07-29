(ns conjure.test.builder
  (:import [java.io File])
  (:require [conjure.test.util :as util]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.loading-utils :as loading-utils]))

(defn
#^{:doc "Finds (or creates if not found) the functional test directory."}
  find-or-create-functional-test-directory
  ([] (find-or-create-functional-test-directory (util/find-test-directory)))
  ([test-directory]
    (if test-directory
      (file-utils/create-dirs test-directory "functional")
      (println "You must pass in a test directory."))))

(defn
#^{:doc "Finds (or creates if not found) the unit test directory."}
  find-or-create-unit-test-directory
  ([] (find-or-create-unit-test-directory (util/find-test-directory)))
  ([test-directory]
    (if test-directory
      (file-utils/create-dirs test-directory "unit")
      (println "You must pass in a test directory."))))

(defn
#^{:doc "Finds (or creates if not found) the view unit test directory."}
  find-or-create-view-unit-test-directory
  ([] (find-or-create-view-unit-test-directory (util/find-test-directory)))
  ([test-directory]
    (if test-directory
      (file-utils/create-dirs test-directory util/unit-dir-name util/unit-view-dir-name)
      (println "You must pass in a test directory."))))

(defn
#^{:doc "Finds (or creates if not found) the unit test directory."}
  find-or-create-controller-view-unit-test-directory
  ([controller] (find-or-create-controller-view-unit-test-directory controller (util/find-test-directory)))
  ([controller test-directory]
    (if test-directory
      (file-utils/create-dirs test-directory util/unit-dir-name util/unit-view-dir-name (loading-utils/dashes-to-underscores controller))
      (println "You must pass in a test directory."))))

(defn
#^{:doc "Finds (or creates if not found) the model unit test directory."}
  find-or-create-model-unit-test-directory
  ([] (find-or-create-model-unit-test-directory (util/find-test-directory)))
  ([test-directory]
    (if test-directory
      (file-utils/create-dirs test-directory util/unit-dir-name util/unit-model-dir-name)
      (println "You must pass in a test directory."))))

(defn
#^{:doc "Creates a new functional test file from the given controller name."}
  create-functional-test
  ([controller-name] (create-functional-test controller-name (find-or-create-functional-test-directory)))
  ([controller-name functional-test-directory]
    (if (and controller-name functional-test-directory)
      (file-utils/create-file (util/functional-test-file controller-name functional-test-directory)))))

(defn
#^{:doc "Creates a new view unit test file from the given controller name and action."}
  create-view-unit-test
  ([controller action] (create-view-unit-test controller action (find-or-create-controller-view-unit-test-directory controller)))
  ([controller action controller-view-unit-test-directory]
    (if (and controller action controller-view-unit-test-directory)
      (file-utils/create-file (util/view-unit-test-file controller action controller-view-unit-test-directory)))))

(defn
#^{:doc "Creates a new model unit test file from the given model."}
  create-model-unit-test
  ([model] (create-model-unit-test model (find-or-create-model-unit-test-directory)))
  ([model model-unit-test-directory]
    (if (and model model-unit-test-directory)
      (file-utils/create-file (util/model-unit-test-file model model-unit-test-directory)))))