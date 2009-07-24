(ns test.server.test-ring-adapter
  (:use clojure.contrib.test-is
        conjure.server.ring-adapter)
  (:require [generators.controller-generator :as controller-generator]
            [destroyers.controller-destroyer :as controller-destroyer]
            [destroyers.view-destroyer :as view-destroyer]))
        
(def controller-name "test")
(def action-name "show")

(defn setup-all [function]
    (controller-generator/generate-controller-file controller-name [ action-name ])
    (function)
    (controller-destroyer/destroy-all-dependencies controller-name [ action-name ]))

(use-fixtures :once setup-all)

(deftest test-call-server
  (call-server 
    { :uri (str controller-name "/" action-name) 
      :query-string "foo=bar&baz=biz" }))
      
(deftest test-conjure
  (conjure 
    { :uri (str controller-name "/" action-name) 
      :query-string "foo=bar&baz=biz" })
  (conjure 
    { :uri "index.html" 
      :query-string "" }))