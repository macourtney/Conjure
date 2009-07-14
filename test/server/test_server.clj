(ns test.server.test-server
  (:use clojure.contrib.test-is
        conjure.server.server)
  (:require [generators.controller-generator :as controller-generator]
            [destroyers.controller-destroyer :as controller-destroyer]
            [destroyers.view-destroyer :as view-destroyer]
            [conjure.controller.util :as controller-util]))

(def controller-name "test")
(def action-name "show")

(defn setup-all [function]
    (controller-generator/generate-controller-file controller-name [ action-name ])
    (function)
    (controller-destroyer/destroy-controller-file controller-name)
    (view-destroyer/destroy-view-file controller-name action-name))
        
(use-fixtures :once setup-all)

(deftest test-parse-query-params
  (is (= { "foo" "bar" } (parse-query-params "foo=bar")))
  (is (= { "foo" "bar", "baz" "biz" } (parse-query-params "foo=bar&baz=biz")))
  (is (= {} (parse-query-params "")))
  (is (= {} (parse-query-params nil))))
  
(deftest test-create-request-map
  (is (= { :controller controller-name, :action action-name, :params { :id "1" }} (create-request-map (str controller-name "/" action-name "/1") {})))
  (is (= { :controller controller-name, :action action-name, :params {}} (create-request-map (str controller-name "/" action-name) {})))
  (is (= { :params {} } (create-request-map controller-name {})))
  (is (= { :params {} } (create-request-map "" {})))
  (is (= { :params {} } (create-request-map nil {})))
  (is (= { :params { :foo "bar" } } (create-request-map "" { :foo "bar" })))
  (is (= { :controller controller-name, :action action-name, :params { :id "1", :foo "bar" } } (create-request-map (str controller-name "/" action-name "/1") { :foo "bar" }))))
  
(deftest test-controller-file-name
  (is (= (str controller-name "_controller.clj") (controller-file-name { :controller controller-name })))
  (is (nil? (controller-file-name { :controller "" })))
  (is (nil? (controller-file-name { :controller nil })))
  (is (nil? (controller-file-name {}))))
  
(deftest test-fully-qualified-action
  (is (= (str "controllers." controller-name "-controller/" action-name) (fully-qualified-action { :controller controller-name, :action action-name })))
  (is (= nil (fully-qualified-action { :controller controller-name })))
  (is (= nil (fully-qualified-action { })))
  (is (= nil (fully-qualified-action nil))))
  
(deftest test-load-controller
  (load-controller (controller-file-name { :controller controller-name })))
    
(deftest test-process-request
  (process-request { :controller controller-name, :action action-name }))
  
(deftest test-render-view
  (render-view { :controller controller-name, :action action-name } ))

(deftest test-http-config
  (is (not (nil? (http-config)))))

(deftest test-db-config
  (is (not (nil? (db-config)))))