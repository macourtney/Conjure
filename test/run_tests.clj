(ns test.run-tests
  (:import [java.io File])
  (:use clojure.contrib.test-is)
  (:require [conjure.util.loading-utils :as loading-utils]
            [clojure.contrib.seq-utils :as seq-utils]
            [clojure.contrib.classpath :as classpath]))

(defn
#^{:doc "Returns a File object for the test_app directory."}
  create-test-app-directory-file []
  (let [conjure-directory (first (classpath/classpath-directories))
        target-directory (new File conjure-directory "target")]
    (new File target-directory "test_app")))
   
(defn
#^{:doc "Returns a File object for the test directory."}
  create-test-directory-file []
  (let [test-app-directory (create-test-app-directory-file)]
    (new File test-app-directory "test")))
   
(defn
#^{:doc "Returns a sequence of all the files found in every directory under the test directory."}
  test-files []
  (let [test-directory (create-test-directory-file)]
    (if test-directory
      (filter 
        (fn [file] (. (. file getPath) endsWith ".clj"))
        (seq-utils/flatten (file-seq test-directory)))
      (do
        (println "test-directory is null")
        []))))
        
(defn
#^{:doc "Returns the namespace as a symbol for the given file which must be found in the test directory."}
  namespace-symbol-for-file [file]
  (let [test-app-path (. (create-test-app-directory-file) getPath)
        file-parent-path (. file getParent)]
    (symbol (loading-utils/namespace-string-for-file (. file-parent-path substring (. test-app-path length)) (. file getName)))))



(let [all-test-files (test-files)]
  (doall (map (fn [file] (if (not (= (. file getName) "run_tests.clj")) (load-file (. file getPath)))) all-test-files))
  (apply run-tests 
    (map namespace-symbol-for-file all-test-files)))