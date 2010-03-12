(ns conjure.test.builder
  (:import [java.io File])
  (:require [clojure.contrib.logging :as logging]
            [conjure.test.util :as util]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.loading-utils :as loading-utils]))

(defn
#^{:doc "Finds (or creates if not found) the functional test directory."}
  find-or-create-functional-test-directory
  ([{ :keys [test-directory silent] :or { test-directory (util/find-test-directory), silent false } }]
    (if test-directory
      (file-utils/create-dirs [test-directory util/functional-dir-name] silent)
      (logging/error "You must pass in a test directory."))))

(defn
#^{:doc "Finds (or creates if not found) the unit test directory."}
  find-or-create-unit-test-directory
  [{ :keys [test-directory silent] :or { test-directory (util/find-test-directory), silent false } }]
    (if test-directory
      (file-utils/create-dirs [ test-directory util/unit-dir-name ] silent)
      (logging/error "You must pass in a test directory.")))

(defn
#^{:doc "Finds (or creates if not found) the view unit test directory."}
  find-or-create-view-unit-test-directory
  [{ :keys [test-directory silent] :or { test-directory (util/find-test-directory), silent false } }]
    (if test-directory
      (file-utils/create-dirs [test-directory util/unit-dir-name util/unit-view-dir-name] silent)
      (logging/error "You must pass in a test directory.")))

(defn
#^{:doc "Finds (or creates if not found) the unit test directory."}
  find-or-create-controller-view-unit-test-directory
  [{ :keys [controller test-directory silent] :or { test-directory (util/find-test-directory), silent false} }]
    (if test-directory
      (file-utils/create-dirs [test-directory util/unit-dir-name util/unit-view-dir-name (loading-utils/dashes-to-underscores controller)] silent)
      (logging/error "You must pass in a test directory.")))

(defn
#^{:doc "Finds (or creates if not found) the model unit test directory."}
  find-or-create-model-unit-test-directory [silent]
    (let [test-directory (util/find-test-directory)] 
      (if test-directory
        (file-utils/create-dirs [test-directory util/unit-dir-name util/unit-model-dir-name] silent)
        (logging/error "You must pass in a test directory."))))
      
(defn
#^{:doc "Finds (or creates if not found) the fixture directory."}
  find-or-create-fixture-directory [silent]
    (let [test-directory (util/find-test-directory)]
      (if test-directory
        (file-utils/create-dirs [test-directory util/fixture-dir-name] silent)
        (logging/error "You must pass in a test directory."))))

(defn
#^{:doc "Creates a new functional test file from the given controller name."}
  create-functional-test [controller-name silent]
    (let [functional-test-directory (find-or-create-functional-test-directory 
                                      { :test-directory (util/find-test-directory),
                                        :silent silent })]
      (if (and controller-name functional-test-directory)
        (file-utils/create-file (util/functional-test-file controller-name functional-test-directory) silent))))

(defn
#^{:doc "Creates a new view unit test file from the given controller name and action."}
  create-view-unit-test
  ([controller action silent]
    (let [controller-view-unit-test-directory 
           (find-or-create-controller-view-unit-test-directory 
             { :controller controller,
               :silent silent })]
      (if (and controller action controller-view-unit-test-directory)
        (file-utils/create-file (util/view-unit-test-file controller action controller-view-unit-test-directory) silent)))))

(defn
#^{:doc "Creates a new model unit test file from the given model."}
  create-model-unit-test [model silent]
    (let [model-unit-test-directory (find-or-create-model-unit-test-directory silent)]
      (if (and model model-unit-test-directory)
        (file-utils/create-file (util/model-unit-test-file model model-unit-test-directory) silent))))

(defn
#^{:doc "Creates a new fixture file from the given model."}
  create-fixture [model silent]
    (let [fixture-directory (find-or-create-fixture-directory silent)]
      (if (and model fixture-directory)
        (file-utils/create-file (util/fixture-file model fixture-directory) silent))))