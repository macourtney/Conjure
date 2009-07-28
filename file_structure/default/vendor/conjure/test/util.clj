(ns conjure.test.util
  (:import [java.io File])
  (:require [conjure.util.loading-utils :as loading-utils]
            [conjure.util.file-utils :as file-utils]))

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
#^{:doc "Finds the functional test directory."}
 find-view-unit-test-directory 
 ([] (find-view-unit-test-directory (find-unit-test-directory)))
 ([unit-test-directory]
   (if unit-test-directory
     (file-utils/find-directory unit-test-directory unit-view-dir-name))))

(defn
  find-controller-view-unit-test-directory
  ([controller] (find-controller-view-unit-test-directory controller (find-view-unit-test-directory)))
  ([controller view-unit-test-directory]
   (if (and controller view-unit-test-directory)
     (file-utils/find-directory view-unit-test-directory (loading-utils/dashes-to-underscores controller)))))

(defn
#^{:doc "Returns the functional test file name for the given controller name."}
  functional-test-file-name [controller]
  (if (and controller (> (. controller length) 0))
    (str (loading-utils/dashes-to-underscores controller) "_controller_test.clj")))

(defn
#^{:doc "Returns the functional test file name for the given controller name."}
  view-unit-test-file-name [action]
  (if (and action (> (. action length) 0))
    (str (loading-utils/dashes-to-underscores action) "_view_test.clj")))
    
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
    (if (and controller (> (. controller length) 0) action (> (. action length) 0) controller-view-unit-test-dir)
      (new File controller-view-unit-test-dir (view-unit-test-file-name action)))))

(defn
#^{:doc "Returns the functional test namespace for the given controller."}
  functional-test-namespace [controller]
  (if (and controller (> (. controller length) 0))
    (str "test.functional." (loading-utils/underscores-to-dashes controller) "-controller-test")))

(defn
#^{:doc "Returns the functional test namespace for the given controller."}
  view-unit-test-namespace [controller action]
  (if (and controller (> (. controller length) 0) action (> (. action length) 0))
    (str "test.unit.view." (loading-utils/underscores-to-dashes controller) "." (loading-utils/underscores-to-dashes action) "-view-test")))