(ns conjure.helper.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.helper.util))
  
(deftest test-find-helpers-directory
  (let [helpers-directory (find-helpers-directory)]
    (is (not (nil? helpers-directory)))
    (is (instance? File helpers-directory))
    (is (= "helpers" (. helpers-directory getName)))
    (is (.exists helpers-directory))))

(deftest test-helper-files
  (doseq [helper-file (helper-files)]
    (is (.isFile helper-file))
    (is (.endsWith (.getName helper-file) ".clj"))))

(deftest test-helper-namespace
  (is (= "helpers.test-helper" (helper-namespace (new File (find-helpers-directory) "test_helper.clj"))))
  (is (= "helpers.submodel.test-helper" (helper-namespace (new File (find-helpers-directory) "submodel/test_helper.clj"))))
  (is (nil? (helper-namespace nil))))

(deftest test-all-helper-namespaces
  (doseq [helper-namespace (all-helper-namespaces)]
    (is helper-namespace)))