(ns test.server.test-ring-adapter
  (:use clojure.contrib.test-is
        conjure.server.ring-adapter)
  (:require [generators.controller-generator :as controller-generator]
            [destroyers.controller-destroyer :as controller-destroyer]
            [destroyers.view-destroyer :as view-destroyer]))
        
(def controller-name "test")
(def action-name "show")
(def uri (str controller-name "/" action-name "/1"))

(defn setup-all [function]
    (controller-generator/generate-controller-file 
      { :controller controller-name, :actions [ action-name ], :silent true })
    (function)
    (controller-destroyer/destroy-all-dependencies 
      { :controller controller-name, :actions [ action-name ], :silent true }))

(use-fixtures :once setup-all)

(deftest test-call-server
  (is
    (call-server 
      { :uri uri
        :query-string "foo=bar&baz=biz" })))
      
(deftest test-conjure
  (is
    (conjure 
      { :uri uri
        :query-string "foo=bar&baz=biz" }))
  (is
    (conjure 
      { :uri "" 
        :query-string "" })))