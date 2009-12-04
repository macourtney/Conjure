(ns test.controller.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.controller.util)
  (:require [generators.controller-generator :as controller-generator]
            [destroyers.controller-destroyer :as controller-destroyer]))


(defn setup-all [function]
  (let [controller-name "test"]
    (controller-generator/generate-controller-file 
      { :controller controller-name, :silent true })
    (function)
    (controller-destroyer/destroy-all-dependencies 
      { :controller controller-name, :silent true })))
        
(use-fixtures :once setup-all)
  
(deftest test-find-controllers-directory
  (let [controllers-directory (find-controllers-directory)]
    (is (not (nil? controllers-directory)))
    (is (instance? File controllers-directory))))
    
(deftest test-controller-file-name-string
  (let [controller-file-name (controller-file-name-string "test")]
    (is (not (nil? controller-file-name)))
    (is (= "test_controller.clj" controller-file-name)))
  (let [controller-file-name (controller-file-name-string "test-name")]
    (is (not (nil? controller-file-name)))
    (is (= "test_name_controller.clj" controller-file-name)))
  (is (nil? (controller-file-name-string nil)))
  (is (nil? (controller-file-name-string ""))))
  
(deftest test-controller-from-file
  (let [controller-file (new File "test_controller.clj")
        controller-name (controller-from-file controller-file)]
    (is (not (nil? controller-name)))
    (is (= "test" controller-name)))
  (let [controller-file (new File "test_name_controller.clj")
        controller-name (controller-from-file controller-file)]
    (is (not (nil? controller-name)))
    (is (= "test-name" controller-name)))
  (is (nil? (controller-from-file nil))))
  
(deftest test-find-controller-file
  (let [controllers-directory (find-controllers-directory)]
    (let [controller-file (find-controller-file controllers-directory "test")]
      (is (not (nil? controller-file))))
    (let [controller-file (find-controller-file controllers-directory "fail")]
      (is (nil? controller-file)))
    (let [controller-file (find-controller-file controllers-directory nil)]
      (is (nil? controller-file)))
  (let [controller-file (find-controller-file "test")]
    (is (not (nil? controller-file))))))
    
(deftest test-controller-namespace
  (let [controller-ns (controller-namespace "test")]
    (is (not (nil? controller-ns)))
    (is (= "controllers.test-controller" controller-ns)))
  (let [controller-ns (controller-namespace "test-name")]
    (is (not (nil? controller-ns)))
    (is (= "controllers.test-name-controller" controller-ns)))
  (let [controller-ns (controller-namespace "test_name")]
    (is (not (nil? controller-ns)))
    (is (= "controllers.test-name-controller" controller-ns)))
  (is (nil? (controller-namespace nil))))