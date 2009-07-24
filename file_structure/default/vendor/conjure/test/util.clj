(ns conjure.test.util
  (:import [java.io File])
  (:require [conjure.util.loading-utils :as loading-utils]
            [conjure.util.file-utils :as file-utils]))

(defn
#^{:doc "Finds the test directory."}
 find-test-directory []
 (loading-utils/get-classpath-dir-ending-with "test"))
 
(defn
#^{:doc "Finds the functional test directory."}
 find-functional-test-directory 
 ([] (find-functional-test-directory (find-test-directory)))
 ([test-directory]
   (if test-directory
     (file-utils/find-directory test-directory "functional"))))

(defn
#^{:doc "Returns the functional test file name for the given controller name."}
  functional-test-file-name [controller]
  (if (and controller (> (. controller length) 0))
    (str (loading-utils/dashes-to-underscores controller) "_controller_test.clj")))
    
(defn
#^{:doc "Returns the functional test file for the given controller name."}
  functional-test-file
  ([controller] (functional-test-file controller (find-functional-test-directory)))
  ([controller functional-test-directory]
    (if (and controller functional-test-directory)
      (new File functional-test-directory (functional-test-file-name controller)))))

(defn
#^{:doc "Returns the functional test namespace for the given controller."}
  functional-test-namespace [controller]
  (if (and controller (> (. controller length) 0))
    (str "test.functional." (loading-utils/underscores-to-dashes controller) "-controller-test")))