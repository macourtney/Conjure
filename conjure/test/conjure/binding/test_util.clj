(ns conjure.binding.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.binding.util
        test-helper)
  (:require [clojure.contrib.logging :as logging]
            [conjure.server.request :as request]))

(def controller-name "test")
(def action-name "show")
(def request-map { :controller controller-name, :action action-name })

(use-fixtures :once init-server)
  
(deftest test-find-bindings-directory
  (let [bindings-directory (find-bindings-directory)]
    (is (not (nil? bindings-directory)))
    (is (instance? File bindings-directory))))
    
(deftest test-binding-file-name-string
  (let [binding-file-name (binding-file-name-string action-name)]
    (is (not (nil? binding-file-name)))
    (is (= (str action-name ".clj") binding-file-name)))
  (let [binding-file-name (binding-file-name-string "test-name")]
    (is (not (nil? binding-file-name)))
    (is (= "test_name.clj" binding-file-name)))
  (is (nil? (binding-file-name-string nil)))
  (is (nil? (binding-file-name-string ""))))
    
(deftest test-binding-namespace
  (let [binding-ns (binding-namespace controller-name action-name)]
    (is (not (nil? binding-ns)))
    (is (= (str "bindings." controller-name "." action-name) binding-ns)))
  (let [binding-ns (binding-namespace "test-name" action-name)]
    (is (not (nil? binding-ns)))
    (is (= (str "bindings.test-name." action-name) binding-ns)))
  (let [binding-ns (binding-namespace controller-name "test_name")]
    (is (not (nil? binding-ns)))
    (is (= (str "bindings." controller-name ".test-name") binding-ns)))
  (is (nil? (binding-namespace nil action-name)))
  (is (nil? (binding-namespace controller-name nil))))

(deftest test-binding-exists?
  (is (binding-exists? controller-name action-name))
  (is (not (binding-exists? "fail" action-name)))
  (is (not (binding-exists? controller-name "fail"))))

(deftest test-load-binding
  (load-binding controller-name action-name))

(deftest test-actions-map
  (is (actions-map controller-name))
  (is (nil? (actions-map "fail")))
  (is (nil? (actions-map nil))))

(deftest test-find-binding-fn
  (is (find-binding-fn controller-name action-name)))

(deftest test-run-binding
  (request/set-request-map request-map
    (is (run-binding controller-name action-name []))))

(deftest test-call-binding
  (request/set-request-map request-map
    (is (call-binding controller-name action-name [])))
  (let [initial-bindings @bindings]
    (reset! bindings {})
    (request/set-request-map request-map
      (is (call-binding controller-name action-name [])))
    (reset! bindings initial-bindings)))

(deftest test-assoc-action
  (let [test-binding (fn [request-map] nil)
        params { :bind-function test-binding }]
    (is (= { (keyword action-name) test-binding } (assoc-action {} (assoc params :action action-name))))
    (is (= { :blah test-binding } (assoc-action {} (assoc params :action :blah))))
    (is (= 
      { (keyword action-name) test-binding, :foo test-binding } 
      (assoc-action { :foo test-binding } (assoc params :action action-name))))))

(deftest test-assoc-controller
  (let [test-binding (fn [request-map] nil)
        params { :action action-name, :bind-function test-binding }
        action-map { (keyword action-name) test-binding }]
    (is (= 
      { (keyword controller-name) action-map } 
      (assoc-controller {} (assoc params :controller controller-name))))
    (is (= 
      { (keyword controller-name) action-map, :foo action-map } 
      (assoc-controller { :foo action-map } (assoc params :controller controller-name))))))

(deftest test-add-bind-function
  (let [initial-bindings @bindings
        test-binding (fn [request-map] nil)
        params { :controller controller-name, :action action-name }
        binding-map { (keyword controller-name) { (keyword action-name) test-binding } }] 
    (reset! bindings {})
    (add-bind-function test-binding params)
    (is (= binding-map @bindings))
    (reset! bindings initial-bindings)))