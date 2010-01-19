(ns test.run-tests
  (:import [java.io File])
  (:use clojure.contrib.test-is)
  (:require environment
            [conjure.server.server :as conjure-server]
            [conjure.util.loading-utils :as loading-utils]
            [clojure.contrib.classpath :as classpath]
            [clojure.contrib.java-utils :as java-utils]
            [clojure.contrib.seq-utils :as seq-utils]))

(defn
#^{:doc "Returns a File object for the test_app directory."}
  create-test-app-directory-file []
  (new File (. (System/getProperties) getProperty "user.dir")))
  
(defn
#^{:doc "Returns a File object for the test directory."}
  create-test-directory-file []
  (new File (create-test-app-directory-file) "test"))
   
(defn
#^{:doc "Returns true if the given file is a valid test file."}
  is-valid-test [test-file]
  (and
    (. (. test-file getPath) endsWith ".clj")
    (not (= (. test-file getName) "run_tests.clj"))
    (not (= (. test-file getName) "test_helper.clj"))))
   
(defn
#^{:doc "Returns a sequence of all the files found in every directory under the test directory."}
  test-files []
  (let [test-directory (create-test-directory-file)]
    (if test-directory
      (filter 
        is-valid-test
        (seq-utils/flatten (file-seq test-directory)))
      (do
        (println "test-directory is null")
        []))))
        
(defn
#^{:doc "Returns the namespace as a symbol for the given file which must be found in the test directory."}
  namespace-symbol-for-file [file]
  (let [test-app-path (. (create-test-app-directory-file) getPath)
        file-parent-path (. file getParent)
        namespace-string (loading-utils/namespace-string-for-file (. file-parent-path substring (. test-app-path length)) (. file getName))]
    (symbol namespace-string)))

(defn
#^{:doc "Initializes Conjure for testing."}
  init []
  (let [initial-value (java-utils/get-system-property environment/conjure-environment-property nil)]
    (if (not initial-value)
      (java-utils/set-system-properties { environment/conjure-environment-property "test" })))
  (println "environment name: " (environment/environment-name))
  (conjure-server/init))

(init)

(if (and *command-line-args* (first *command-line-args*) (> (.length (first *command-line-args*)) 0))
  (do
    (println "Running scripts:" *command-line-args*)
    (doall (map 
      (fn [namespace-str] 
        (load-file (str "./" (loading-utils/symbol-string-to-clj-file namespace-str)))) 
      *command-line-args*))
    (apply run-tests (map symbol *command-line-args*)))
  (let [all-test-files (test-files)]
    (doall (map (fn [file] (if (not (= (. file getName) "run_tests.clj")) (load-file (. file getPath)))) all-test-files))
    (apply run-tests 
      (map namespace-symbol-for-file all-test-files))))