(ns conjure.test.util
  (:import [java.io File])
  (:require [conjure.util.file-utils :as file-utils]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.string-utils :as conjure-str-utils]))

(def test-dir-name "test")
(def functional-dir-name "functional")
(def unit-dir-name "unit")
(def unit-view-dir-name "view")
(def unit-model-dir-name "model")
(def fixture-dir-name "fixture")

(defn
#^{:doc "Finds the test directory."}
 find-test-directory []
 (loading-utils/get-classpath-dir-ending-with test-dir-name))
 
(defn
#^{:doc "Finds the functional test directory."}
 find-functional-test-directory 
 ([] (find-functional-test-directory (find-test-directory)))
 ([test-directory]
   (if test-directory
     (file-utils/find-directory test-directory functional-dir-name))))

(defn
#^{:doc "Finds the functional test directory."}
 find-unit-test-directory 
 ([] (find-unit-test-directory (find-test-directory)))
 ([test-directory]
   (if test-directory
     (file-utils/find-directory test-directory unit-dir-name))))

(defn
#^{:doc "Finds the view test directory."}
 find-view-unit-test-directory 
 ([] (find-view-unit-test-directory (find-unit-test-directory)))
 ([unit-test-directory]
   (if unit-test-directory
     (file-utils/find-directory unit-test-directory unit-view-dir-name))))

(defn
#^{:doc "Finds the view controller test directory."}
  find-controller-view-unit-test-directory
  ([controller] (find-controller-view-unit-test-directory controller (find-view-unit-test-directory)))
  ([controller view-unit-test-directory]
   (if (and controller view-unit-test-directory)
     (file-utils/find-directory view-unit-test-directory (loading-utils/dashes-to-underscores controller)))))

(defn
#^{:doc "Finds the model test directory."}
 find-model-unit-test-directory 
 ([] (find-model-unit-test-directory (find-unit-test-directory)))
 ([unit-test-directory]
   (if unit-test-directory
     (file-utils/find-directory unit-test-directory unit-model-dir-name))))

(defn
#^{:doc "Finds the fixture directory."}
 find-fixture-directory 
 ([] (find-fixture-directory (find-test-directory)))
 ([test-directory]
   (if test-directory
     (file-utils/find-directory test-directory fixture-dir-name))))

(defn
#^{:doc "Returns the functional test file name for the given controller name."}
  functional-test-file-name [controller]
  (if (and controller (> (. controller length) 0))
    (str (loading-utils/dashes-to-underscores controller) "_controller_test.clj")))

(defn
#^{:doc "Returns the view test file name for the given action name."}
  view-unit-test-file-name [action]
  (if (and action (> (. action length) 0))
    (str (loading-utils/dashes-to-underscores action) "_view_test.clj")))

(defn
#^{:doc "Returns the model test file name for the given model name."}
  model-unit-test-file-name [model]
  (if (and model (> (. model length) 0))
    (str (loading-utils/dashes-to-underscores model) "_model_test.clj")))

(defn
#^{:doc "Returns the fixture file name for the given model name."}
  fixture-file-name [model]
  (if (and model (> (. model length) 0))
    (str (loading-utils/dashes-to-underscores model) ".clj")))
    
(defn
#^{:doc "Returns the functional test file for the given controller name."}
  functional-test-file
  ([controller] (functional-test-file controller (find-functional-test-directory)))
  ([controller functional-test-directory]
    (if (and controller (> (. controller length) 0) functional-test-directory)
      (new File functional-test-directory (functional-test-file-name controller)))))

(defn
#^{:doc "Returns the functional test file for the given controller name."}
  view-unit-test-file
  ([controller action] (view-unit-test-file controller action (find-controller-view-unit-test-directory controller)))
  ([controller action controller-view-unit-test-dir]
    (let [controller-str (conjure-str-utils/str-keyword controller)
          action-str (conjure-str-utils/str-keyword action)]
      (if (and controller-str (> (. controller-str length) 0) action-str (> (. action-str length) 0) controller-view-unit-test-dir)
        (new File controller-view-unit-test-dir (view-unit-test-file-name action-str))))))

(defn
#^{:doc "Returns the functional test file for the given controller name."}
  model-unit-test-file
  ([model] (model-unit-test-file model (find-model-unit-test-directory)))
  ([model model-unit-test-dir]
    (if (and model (> (. model length) 0) model-unit-test-dir)
      (new File model-unit-test-dir (model-unit-test-file-name model)))))

(defn
#^{:doc "Returns the fixture file for the given model name."}
  fixture-file
  ([model] (fixture-file model (find-fixture-directory)))
  ([model fixture-directory]
    (if (and model (> (. model length) 0) fixture-directory)
      (new File fixture-directory (fixture-file-name model)))))

(defn
#^{:doc "Returns the functional test namespace for the given controller."}
  functional-test-namespace [controller]
  (if (and controller (> (. controller length) 0))
    (str "functional." (loading-utils/underscores-to-dashes controller) "-controller-test")))

(defn
#^{:doc "Returns the view test namespace for the given controller and action."}
  view-unit-test-namespace [controller action]
  (let [controller-str (conjure-str-utils/str-keyword controller)
        action-str (conjure-str-utils/str-keyword action)]
    (if (and controller-str (> (. controller-str length) 0) action-str (> (. action-str length) 0))
      (str 
        "unit.view." 
        (loading-utils/underscores-to-dashes controller-str) 
        "." 
        (loading-utils/underscores-to-dashes action-str) 
        "-view-test"))))

(defn
#^{:doc "Returns the model test namespace for the given model."}
  model-unit-test-namespace [model]
  (if (and model (> (. model length) 0))
    (str "unit.model." (loading-utils/underscores-to-dashes model) "-model-test")))

(defn
#^{:doc "Returns the fixture namespace for the given model."}
  fixture-namespace [model]
  (if (and model (> (. model length) 0))
    (str "fixture." (loading-utils/underscores-to-dashes model))))