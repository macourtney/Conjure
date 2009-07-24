(ns conjure.test.builder
  (:import [java.io File])
  (:require [conjure.test.util :as util]))

(defn
#^{:doc "Finds (or creates if not found) the functional test directory."}
  find-or-create-functional-test-directory
  ([] (find-or-create-functional-test-directory (util/find-test-directory)))
  ([test-directory]
    (if test-directory
      (let [functional-directory (util/find-functional-test-directory test-directory)]
        (if functional-directory
          (do
            (println (. functional-directory getPath) "directory already exists.")
            functional-directory)
          (do
            (println "Creating functional directory in test...")
            (let [new-functional-directory (new File test-directory "functional")]
              (. new-functional-directory mkdirs)
              new-functional-directory))))
      (println "You must pass in a test directory."))))

(defn
#^{:doc "Creates a new functional test file from the given controller name."}
  create-functional-test
  ([controller-name] (create-functional-test controller-name (find-or-create-functional-test-directory)))
  ([controller-name functional-test-directory]
    (if (and controller-name functional-test-directory)
      (let [functional-test-file (util/functional-test-file controller-name functional-test-directory)]
        (if (. functional-test-file exists)
          (println (. functional-test-file getName) "already exists. Doing nothing.")
          (do
            (println "Creating functional test file" (. functional-test-file getName) "...")
            (. functional-test-file createNewFile)
            functional-test-file))))))