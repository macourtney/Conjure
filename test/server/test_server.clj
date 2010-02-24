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
  (is (= 
    { :foo "bar" }
    (parse-post-params
      { :request 
        { :request-method :post,
          :content-type "application/x-www-form-urlencoded",
          :content-length 7,
          :body (string-as-input-stream "foo=bar") } })))
  (is (= 
    { :foo "bar", :biz "baz" }
    (parse-post-params
      { :request 
        { :request-method :post,
          :content-type "application/x-www-form-urlencoded",
          :content-length 15,
          :body (string-as-input-stream "foo=bar&biz=baz") } })))
  (is (= 
    {} 
    (parse-post-params 
      { :request-method :get, 
        :content-length 7, 
        :body (string-as-input-stream "foo=bar") })))
  (is (= 
    {} 
    (parse-post-params 
      { :request-method :post,
        :content-type "multipart/form-data",
        :content-length 7, 
        :body (string-as-input-stream "foo=bar") }))))

(deftest test-parse-params
  (is (= 
    { :foo "bar", :biz "baz" }
    (parse-params 
      { :request 
        { :request-method :post,
          :content-type "application/x-www-form-urlencoded",
          :content-length 7,
          :body (string-as-input-stream "foo=bar"),
          :query-string "biz=baz" } })))
  (is (= { :biz "baz" } (parse-params { :request { :query-string "biz=baz" } } )))
  (is (= 
    { :foo "bar" } 
    (parse-params 
      { :request 
        { :request-method :post,
          :content-type "application/x-www-form-urlencoded",
          :content-length 7,
          :body (string-as-input-stream "foo=bar") } } )))
  (is (= {} (parse-params {} ))))

(deftest test-update-request-map
  (let [headers { "cookie" "SID=blah" }]
    (let [uri (str "/" controller-name "/" action-name "/1")]
      (is (= 
        { :controller controller-name,
          :action action-name,
          :params { :id "1" }
          :request 
          { :uri uri,
            :headers headers } }
        (update-request-map 
          { :request 
            { :uri uri, 
              :headers headers } }))))
            
    (let [uri (str "/" controller-name "/" action-name)]
      (is (= 
        { :controller controller-name,
          :action action-name,
          :params {}
          :request 
          { :uri uri
            :headers headers } }
        (update-request-map 
          { :request 
            { :uri uri
              :headers headers } }))))
      
    (is (= 
        { :params {}, 
          :action "index", 
          :controller "test", 
          :request 
          { :uri "test"
            :headers headers } }
        (update-request-map 
          { :request 
            { :uri controller-name
              :headers headers } })))
          
    (is (= 
      { :params {}, 
        :action "index", 
        :controller "home", 
        :request 
        { :uri ""
          :headers headers } } 
      (update-request-map 
        { :request 
          { :uri ""
            :headers headers } })))
        
    (let [request-map (update-request-map nil)]
      (is (= {} (:params request-map)))
      (is (contains? request-map :temp-session)))
      
    (is (=
      { :params { :foo "bar" },
        :action "index",
        :controller "home",
        :request 
        { :uri "",
          :query-string "foo=bar"
          :headers headers } }
      (update-request-map 
        { :request 
          { :uri "",
            :query-string "foo=bar"
            :headers headers } })))
          
    (let [uri (str "/" controller-name "/" action-name "/1")]
      (is (= 
        { :controller controller-name, 
          :action action-name, 
          :params { :id "1", :foo "bar" }, 
          :request 
          { :uri uri, 
            :query-string "foo=bar"
            :headers headers } }
        (update-request-map 
          { :request 
            { :uri uri,
              :query-string "foo=bar"
              :headers headers } }))))))
      
(deftest test-process-request
  (process-request { :controller controller-name, :action action-name }))
  
(deftest test-http-config
  (is (not (nil? (http-config)))))

(deftest test-db-config
  (is (not (nil? (db-config)))))