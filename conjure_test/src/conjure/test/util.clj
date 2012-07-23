(ns conjure.test.util
  (:import [java.io File])
  (:require [conjure.config.environment :as environment]
            [clojure.tools.file-utils :as file-utils]
            [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.string-utils :as conjure-str-utils]))

(def test-dir-name "test")
(def functional-dir-name "functional")
(def unit-dir-name "unit")
(def unit-view-dir-name "view")
(def unit-model-dir-name "model")
(def unit-binding-dir-name "binding")
(def fixture-dir-name "fixture")
(def unit-binding-dir-name "binding")

(defn
#^{:doc "Finds the functional test directory."}
 find-functional-test-directory 
 ([] (find-functional-test-directory (environment/find-test-dir)))
 ([test-directory]
   (when test-directory
     (file-utils/find-directory test-directory functional-dir-name))))

(defn
#^{:doc "Finds the functional test directory."}
 find-unit-test-directory 
 ([] (find-unit-test-directory (environment/find-test-dir)))
 ([test-directory]
   (when test-directory
     (file-utils/find-directory test-directory unit-dir-name))))

(defn
#^{:doc "Finds the view test directory."}
 find-view-unit-test-directory 
 ([] (find-view-unit-test-directory (find-unit-test-directory)))
 ([unit-test-directory]
   (when unit-test-directory
     (file-utils/find-directory unit-test-directory unit-view-dir-name))))

(defn
#^{:doc "Finds the view service test directory."}
  find-service-view-unit-test-directory
  ([service] (find-service-view-unit-test-directory service (find-view-unit-test-directory)))
  ([service view-unit-test-directory]
   (when (and service view-unit-test-directory)
     (file-utils/find-directory view-unit-test-directory (loading-utils/dashes-to-underscores service)))))

(defn
#^{:doc "Finds the model test directory."}
 find-model-unit-test-directory 
 ([] (find-model-unit-test-directory (find-unit-test-directory)))
 ([unit-test-directory]
   (when unit-test-directory
     (file-utils/find-directory unit-test-directory unit-model-dir-name))))

(defn
#^{:doc "Finds the fixture directory."}
 find-fixture-directory 
 ([] (find-fixture-directory (environment/find-test-dir)))
 ([test-directory]
   (when test-directory
     (file-utils/find-directory test-directory fixture-dir-name))))

(defn
#^{:doc "Returns the functional test file name for the given service name."}
  functional-test-file-name [service]
  (when (not-empty service)
    (str (loading-utils/dashes-to-underscores service) "_flow_test.clj")))

(defn
#^{:doc "Returns the view test file name for the given action name."}
  view-unit-test-file-name [action]
  (when (not-empty action)
    (str (loading-utils/dashes-to-underscores action) "_view_test.clj")))

(defn
#^{:doc "Returns the model test file name for the given model name."}
  model-unit-test-file-name [model]
  (when (not-empty model)
    (str (loading-utils/dashes-to-underscores model) "_model_test.clj")))

(defn
#^{:doc "Returns the binding test file name for the given action name."}
  binding-unit-test-file-name [action]
  (when (not-empty action)
    (str (loading-utils/dashes-to-underscores action) "_binding_test.clj")))

(defn
#^{:doc "Returns the fixture file name for the given model name."}
  fixture-file-name [model]
  (when (not-empty model)
    (str (loading-utils/dashes-to-underscores model) ".clj")))

(defn
#^{:doc "Returns the view test file name for the given action name."}
  binding-unit-test-file-name [action]
  (when (not-empty action)
    (str (loading-utils/dashes-to-underscores action) "_binding_test.clj")))
    
(defn
#^{:doc "Returns the functional test file for the given service name."}
  functional-test-file
  ([service] (functional-test-file service (find-functional-test-directory)))
  ([service functional-test-directory]
    (when (and (not-empty service) functional-test-directory)
      (new File functional-test-directory (functional-test-file-name service)))))

(defn
#^{:doc "Returns the view unit test file for the given service name."}
  view-unit-test-file
  ([service action] (view-unit-test-file service action (find-service-view-unit-test-directory service)))
  ([service action service-view-unit-test-dir]
    (let [service-str (conjure-str-utils/str-keyword service)
          action-str (conjure-str-utils/str-keyword action)]
      (when (and (not-empty service-str) (not-empty action-str) service-view-unit-test-dir)
        (new File service-view-unit-test-dir (view-unit-test-file-name action-str))))))

(defn
#^{:doc "Returns the model unit test file for the given service name."}
  model-unit-test-file
  ([model] (model-unit-test-file model (find-model-unit-test-directory)))
  ([model model-unit-test-dir]
    (when (and (not-empty model) model-unit-test-dir)
      (new File model-unit-test-dir (model-unit-test-file-name model)))))

(defn
#^{:doc "Returns the fixture file for the given model name."}
  fixture-file
  ([model] (fixture-file model (find-fixture-directory)))
  ([model fixture-directory]
    (when (and (not-empty model) fixture-directory)
      (new File fixture-directory (fixture-file-name model)))))

(defn
#^{:doc "Returns the functional test namespace for the given service."}
  functional-test-namespace [service]
  (when (not-empty service)
    (str "functional." (loading-utils/underscores-to-dashes service) "-flow-test")))

(defn
#^{:doc "Returns the view test namespace for the given service and action."}
  view-unit-test-namespace [service action]
  (let [service-str (conjure-str-utils/str-keyword service)
        action-str (conjure-str-utils/str-keyword action)]
    (when (and (not-empty service-str) (not-empty action-str))
      (str 
        "unit." unit-view-dir-name "." 
        (loading-utils/underscores-to-dashes service-str) 
        "." 
        (loading-utils/underscores-to-dashes action-str) 
        "-view-test"))))

(defn
#^{:doc "Returns the model test namespace for the given model."}
  model-unit-test-namespace [model]
  (when (not-empty model)
    (str "unit." unit-model-dir-name "." (loading-utils/underscores-to-dashes model) "-model-test")))

(defn
#^{:doc "Returns the fixture namespace for the given model."}
  fixture-namespace [model]
  (when (not-empty model)
    (str "fixture." (loading-utils/underscores-to-dashes model))))

(defn
#^{:doc "Returns the binding test namespace for the given service and action."}
  binding-unit-test-namespace [service action]
  (let [service-str (conjure-str-utils/str-keyword service)
        action-str (conjure-str-utils/str-keyword action)]
    (when (and (not-empty service-str) (not-empty action-str))
      (str 
        "unit." unit-binding-dir-name "." 
        (loading-utils/underscores-to-dashes service-str) 
        "." 
        (loading-utils/underscores-to-dashes action-str) 
        "-binding-test"))))