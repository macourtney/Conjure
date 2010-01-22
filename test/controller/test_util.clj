(ns test.controller.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.controller.util)
  (:require [generators.controller-generator :as controller-generator]
            [destroyers.controller-destroyer :as controller-destroyer]))

(def controller-name "test")
(def action-name "blah")

(defn setup-all [function]
  (controller-generator/generate-controller-file 
    { :controller controller-name, :silent true })
  (function)
  (controller-destroyer/destroy-all-dependencies 
    { :controller controller-name, :silent true }))
        
(use-fixtures :once setup-all)
  
(deftest test-find-controllers-directory
  (let [controllers-directory (find-controllers-directory)]
    (is (not (nil? controllers-directory)))
    (is (instance? File controllers-directory))))
    
(deftest test-controller-file-name-string
  (let [controller-file-name (controller-file-name-string controller-name)]
    (is (not (nil? controller-file-name)))
    (is (= "test_controller.clj" controller-file-name)))
  (let [controller-file-name (controller-file-name-string "test-name")]
    (is (not (nil? controller-file-name)))
    (is (= "test_name_controller.clj" controller-file-name)))
  (is (nil? (controller-file-name-string nil)))
  (is (nil? (controller-file-name-string ""))))

(deftest test-controller-file-name
  (is (= (str controller-name "_controller.clj") (controller-file-name { :controller controller-name })))
  (is (nil? (controller-file-name { :controller "" })))
  (is (nil? (controller-file-name { :controller nil })))
  (is (nil? (controller-file-name {}))))
  
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
    (let [controller-file (find-controller-file controllers-directory controller-name)]
      (is (not (nil? controller-file))))
    (let [controller-file (find-controller-file controllers-directory "fail")]
      (is (nil? controller-file)))
    (let [controller-file (find-controller-file controllers-directory nil)]
      (is (nil? controller-file)))
  (let [controller-file (find-controller-file "test")]
    (is (not (nil? controller-file))))))
    
(deftest test-controller-namespace
  (let [controller-ns (controller-namespace controller-name)]
    (is (not (nil? controller-ns)))
    (is (= "controllers.test-controller" controller-ns)))
  (let [controller-ns (controller-namespace "test-name")]
    (is (not (nil? controller-ns)))
    (is (= "controllers.test-name-controller" controller-ns)))
  (let [controller-ns (controller-namespace "test_name")]
    (is (not (nil? controller-ns)))
    (is (= "controllers.test-name-controller" controller-ns)))
  (is (nil? (controller-namespace nil))))

(deftest test-controller-exists?
  (is (controller-exists? (controller-file-name { :controller controller-name })))
  (is (not (controller-exists? (controller-file-name { :controller "fail" })))))

(deftest test-load-controller
  (load-controller (controller-file-name { :controller controller-name })))

(deftest test-fully-qualified-action
  (is (= (str "controllers." controller-name "-controller/" action-name) (fully-qualified-action { :controller controller-name, :action action-name })))
  (is (= nil (fully-qualified-action { :controller controller-name })))
  (is (= nil (fully-qualified-action { })))
  (is (= nil (fully-qualified-action nil))))