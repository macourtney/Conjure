(ns test.server.test-server
  (:import [java.io ByteArrayInputStream])
  (:use clojure.contrib.test-is
        conjure.server.server)
  (:require [generators.controller-generator :as controller-generator]
            [destroyers.controller-destroyer :as controller-destroyer]
            [destroyers.view-destroyer :as view-destroyer]
            [conjure.controller.util :as controller-util]
            [conjure.test.util :as test-util]))

(def controller-name "test")
(def action-name "show")

(defn setup-all [function]
    (controller-generator/generate-controller-file 
      { :controller controller-name, :actions [ action-name ], :silent true })
    (function)
    (controller-destroyer/destroy-all-dependencies 
      { :controller controller-name, :actions [ action-name ], :silent true }))
        
(use-fixtures :once setup-all)

(deftest test-augment-params
  (is (= {:params { :foo "bar" } } (augment-params { :params {} } { :foo "bar" })))
  (is (= {:params { :foo "bar", :biz "baz" } } (augment-params { :params { :biz "baz" } } { :foo "bar" })))
  (is (= {:params { :foo "bar" } } (augment-params { :params { :foo "bar" } } {})))
  (is (= {:params { :foo "bar" } } (augment-params { :params { :foo "bar" } } nil)))
  (is (nil? (augment-params nil { :foo "bar" } ))))

(defn
#^{ :doc "Converts the given string into an input stream. Assumes the character incoding is UTF-8." }
  string-as-input-stream [string]
  (new ByteArrayInputStream (. string getBytes "UTF-8")))

(deftest test-parse-post-params
  (is (= { :foo "bar" } (parse-post-params { :request-method :post, :content-length 7, :body (string-as-input-stream "foo=bar") })))
  (is (= { :foo "bar", :biz "baz" } (parse-post-params { :request-method :post, :content-length 15, :body (string-as-input-stream "foo=bar&biz=baz") })))
  (is (= {} (parse-post-params { :request-method :get, :content-length 7, :body (string-as-input-stream "foo=bar") }))))

(deftest test-parse-params
  (is (= { :foo "bar", :biz "baz" } (parse-params { :request-method :post, :content-length 7, :body (string-as-input-stream "foo=bar"), :query-string "biz=baz" } )))
  (is (= { :biz "baz" } (parse-params { :query-string "biz=baz" } )))
  (is (= { :foo "bar" } (parse-params { :request-method :post, :content-length 7, :body (string-as-input-stream "foo=bar") } )))
  (is (= {} (parse-params {} ))))

(deftest test-update-request-map
  (let [uri (str "/" controller-name "/" action-name "/1")]
    (is (= { :controller controller-name, :action action-name, :params { :id "1" } :uri uri } (update-request-map { :uri uri }))))
  (let [uri (str "/" controller-name "/" action-name)]
    (is (= { :controller controller-name, :action action-name, :params {} :uri uri } (update-request-map { :uri uri }))))
  (is (= { :params {}, :action "index", :controller "test", :uri "test" } (update-request-map { :uri controller-name })))
  (is (= { :params {}, :action "index", :controller "home", :uri ""} (update-request-map { :uri "" })))
  (is (= { :params {} } (update-request-map nil)))
  (is (= { :params { :foo "bar" }, :action "index", :controller "home", :uri "", :query-string "foo=bar" } (update-request-map { :uri "" :query-string "foo=bar" })))
  (let [uri (str "/" controller-name "/" action-name "/1")]
    (is (= { :controller controller-name, :action action-name, :params { :id "1", :foo "bar" }, :uri uri, :query-string "foo=bar" } (update-request-map { :uri uri, :query-string "foo=bar" })))))
  
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