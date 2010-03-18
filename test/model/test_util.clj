(ns test.model.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.model.util)
  (:require [generators.model-generator :as model-generator]
            [destroyers.model-destroyer :as model-destroyer]))

(def model-name "test")

(defn setup-all [function]
  (model-generator/generate-model-file model-name)
  (function)
  (model-destroyer/destroy-all-dependencies model-name))
        
(use-fixtures :once setup-all)

(deftest test-model-from-file
  (is (= model-name (model-from-file (new File (str "models/" model-name ".clj")))))
  (is (nil? (model-from-file nil))))
  
(deftest test-model-namespace
  (is (= (str "models." model-name) (model-namespace model-name)))
  (is (nil? (model-namespace nil))))
  
(deftest test-find-models-directory
  (let [models-dirctory (find-models-directory)]
    (is (not (nil? models-dirctory)))
    (is (instance? File models-dirctory))
    (is (= "models" (. models-dirctory getName)))))

(deftest test-model-files
  (doseq [model-file (model-files)]
    (is (.isFile model-file))
    (is (.endsWith (.getName model-file) ".clj"))))

(deftest test-model-file-namespace
  (is (= "models.test-model" (model-file-namespace (new File (find-models-directory) "test_model.clj"))))
  (is (= "models.submodel.test-model" (model-file-namespace (new File (find-models-directory) "submodel/test_model.clj"))))
  (is (nil? (model-file-namespace nil))))

(deftest test-all-model-namespaces
  (doseq [model-namespace (all-model-namespaces)]
    (is model-namespace)
    (is (symbol? model-namespace))))
    
(deftest test-model-file-name-string
  (is (= (str model-name ".clj") (model-file-name-string model-name)))
  (is (= "test_foo.clj" )(model-file-name-string "test-foo"))
  (is (= "test_foo.clj" )(model-file-name-string "test_foo"))
  (is (nil? (model-file-name-string nil))))
  
(deftest test-migration-for-model
  (let [migration-file-name (migration-for-model model-name)]
    (is (not (nil? migration-file-name)))
    (is (= "create-tests" migration-file-name)))
  (is (nil? (migration-for-model nil))))
  
(deftest test-model-to-table-name
  (is (= "tests" (model-to-table-name model-name)))
  (is (= "test_foos" (model-to-table-name "test-foo")))
  (is (= "test_foos" (model-to-table-name "test_foo")))
  (is (nil? (model-to-table-name nil))))
  
(deftest test-find-model-file
  (let [model-file (find-model-file (find-models-directory) model-name)]
    (is (not (nil? model-file)))
    (is (instance? File model-file))
    (is (= "test.clj" (. model-file getName))))
  (let [model-file (find-model-file model-name)]
    (is (not (nil? model-file)))
    (is (instance? File model-file))
    (is (= "test.clj" (. model-file getName))))
  (is (nil? (find-model-file nil)))
  (is (nil? (find-model-file nil model-name)))
  (is (nil? (find-model-file nil nil))))

(deftest test-to-model-name
  (is (= "puppy" (to-model-name "puppy-id")))
  (is (= "puppy" (to-model-name "puppy")))
  (is (= "" (to-model-name "")))
  (is (= nil (to-model-name nil))))