(ns test.view.test-util
  (:import [java.io File])
  (:use test-helper
        clojure.contrib.test-is
        conjure.view.util)
  (:require [generators.view-generator :as view-generator]
            [destroyers.view-destroyer :as view-destroyer]))

(def action-name "show")
(def controller-name "test")

(defn setup-all [function]
  (view-generator/generate-view-file controller-name action-name)
  (function)
  (view-destroyer/destroy-view-file controller-name action-name))
        
(use-fixtures :once setup-all)

(deftest test-find-views-directory
  (test-directory (find-views-directory) "views"))
    
(deftest test-find-controller-directory
  (test-directory (find-controller-directory (find-views-directory) controller-name) controller-name)
  (test-directory (find-controller-directory controller-name) controller-name)
  (is (nil? (find-controller-directory nil))))
  
(deftest test-find-view-file
  (let [controller-directory (find-controller-directory controller-name)]
    (test-file 
      (find-view-file controller-directory action-name) 
      (str action-name ".clj"))
    (is (nil? (find-view-file controller-directory nil)))
    (is (nil? (find-view-file nil action-name)))
    (is (nil? (find-view-file nil nil)))))
    
(deftest test-load-view
  (load-view { :controller controller-name
               :action action-name }))

(deftest test-request-view-namespace
  (is (= (str "views." controller-name "." action-name) 
         (request-view-namespace { :controller controller-name
                                   :action action-name })))
  (is (= "views.test-foo.show-foo" 
         (request-view-namespace { :controller "test-foo"
                                   :action "show-foo" })))
  (is (= "views.test-foo.show-foo" 
         (request-view-namespace { :controller "test_foo"
                                   :action "show_foo" })))
  (is (nil? (request-view-namespace nil))))
  
(deftest test-view-namespace
  (is (= (str "views." controller-name "." action-name)
         (view-namespace controller-name (new File (str action-name ".clj")))))
  (is (= "views.test-foo.show-foo"
         (view-namespace "test-foo" (new File "show-foo.clj"))))
  (is (= "views.test-foo.show-foo"
         (view-namespace "test_foo" (new File "show_foo.clj"))))
  (is (nil? (view-namespace controller-name nil)))
  (is (nil? (view-namespace nil (new File "show-foo.clj"))))
  (is (nil? (view-namespace nil nil))))