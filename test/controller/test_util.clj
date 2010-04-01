(ns test.controller.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.controller.util)
  (:require [generators.controller-generator :as controller-generator]
            [destroyers.controller-destroyer :as controller-destroyer]
            [clojure.contrib.logging :as logging]))

(def controller-name "test")
(def action-name "blah")

(defn setup-all [function]
  (let [generator-map { :controller controller-name, :actions [action-name], :silent true }]
    (controller-generator/generate-controller-file generator-map)
    (load-controller controller-name)
    (function)
    (controller-destroyer/destroy-all-dependencies generator-map)))
        
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
  (load-controller controller-name))

(deftest test-fully-qualified-action
  (is (= (str "controllers." controller-name "-controller/" action-name) (fully-qualified-action { :controller controller-name, :action action-name })))
  (is (= nil (fully-qualified-action { :controller controller-name })))
  (is (= nil (fully-qualified-action { })))
  (is (= nil (fully-qualified-action nil))))

(deftest test-method-key
  (is (= :get (method-key { :request { :method "GET" } })))
  (is (= :post (method-key { :request { :method "POST" } })))
  (is (= :put (method-key { :request { :method "PUT" } })))
  (is (= :delete (method-key { :request { :method "DELETE" } }))))

(deftest test-actions-map
  (is (actions-map controller-name))
  (is (nil? (actions-map "fail")))
  (is (nil? (actions-map nil))))

(deftest test-methods-map
  (is (methods-map controller-name action-name))
  (is (nil? (methods-map "fail" action-name)))
  (is (nil? (methods-map nil action-name)))
  (is (nil? (methods-map controller-name "fail"))))

(deftest test-action-function
  (is (action-function controller-name action-name :all))
  (is (action-function controller-name action-name))
  (is (action-function controller-name action-name :default))
  (is (nil? (action-function controller-name "fail" :all)))
  (is (nil? (action-function "fail" action-name :all))))

(deftest test-find-action-fn
  (is (find-action-fn { :controller controller-name, :action action-name, :request { :method "GET" } })))

(deftest test-run-action
  (is (run-action { :controller controller-name, :action action-name, :request { :method "GET" } })))

(deftest test-call-controller
  (is (call-controller { :controller controller-name, :action action-name, :request { :method "GET" } }))
  (let [initial-controller-actions @controller-actions]
    (reset! controller-actions {})
    (is (call-controller { :controller controller-name, :action action-name, :request { :method "GET" } }))
    (reset! controller-actions initial-controller-actions)))

(deftest test-assoc-methods
  (let [test-action (fn [request-map] nil)
        params { :action-function test-action }]
    (is (= { :all test-action } (assoc-methods {} (assoc params :methods [:all]))))
    (is (= { :get test-action } (assoc-methods {} (assoc params :methods [:get]))))
    (is (= 
      { :get test-action, :put test-action } 
      (assoc-methods {} (assoc params :methods [:get :put]))))
    (is (= 
      { :get test-action, :put test-action } 
      (assoc-methods { :get test-action } (assoc params :methods [:put]))))
    (is (= {} (assoc-methods {} (assoc params :methods []))))
    (is (= { :all test-action } (assoc-methods {} params)))))

(deftest test-assoc-actions
  (let [test-action (fn [request-map] nil)
        params { :action-function test-action, :methods [:all] }
        method-map { :all test-action }]
    (is (= { (keyword action-name) method-map } (assoc-actions {} (assoc params :action action-name))))
    (is (= { :blah method-map } (assoc-actions {} (assoc params :action :blah))))
    (is (= 
      { (keyword action-name) method-map, :foo method-map } 
      (assoc-actions { :foo method-map } (assoc params :action action-name))))))

(deftest test-assoc-controllers
  (let [test-action (fn [request-map] nil)
        params { :action action-name, :action-function test-action, :methods [:all] }
        action-map { (keyword action-name) { :all test-action } }]
    (is (= 
      { (keyword controller-name) action-map } 
      (assoc-controllers {} (assoc params :controller controller-name))))
    (is (= 
      { (keyword controller-name) action-map, :foo action-map } 
      (assoc-controllers { :foo action-map } (assoc params :controller controller-name))))))

(deftest test-add-action-function
  (let [initial-controller-actions @controller-actions
        test-action (fn [request-map] nil)
        params { :controller controller-name, :action action-name }
        controller-map { (keyword controller-name) { (keyword action-name) { :all test-action } } }] 
    (reset! controller-actions {})
    (add-action-function test-action (assoc params :methods [:all]))
    (is (= controller-map @controller-actions))
    (reset! controller-actions {})
    (add-action-function test-action params)
    (is (= controller-map @controller-actions))
    (reset! controller-actions initial-controller-actions)))