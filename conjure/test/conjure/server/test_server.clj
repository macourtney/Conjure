(ns conjure.server.test-server
  (:import [java.io ByteArrayInputStream])
  (:use clojure.contrib.test-is
        conjure.server.server)
  (:require 
            ;[generators.controller-generator :as controller-generator]
            ;[destroyers.controller-destroyer :as controller-destroyer]
            ;[destroyers.view-destroyer :as view-destroyer]
            [conjure.controller.util :as controller-util]
            [conjure.test.util :as test-util]))

(def controller-name "test")
(def action-name "show")

(defn setup-all [function]
    ;(controller-generator/generate-controller-file 
    ;  { :controller controller-name, :actions [ action-name ], :silent true })
    (function)
    ;(controller-destroyer/destroy-all-dependencies 
    ;  { :controller controller-name, :actions [ action-name ], :silent true })
    )

(use-fixtures :once setup-all)

(deftest test-process-request
  (process-request { :controller controller-name, :action action-name }))
  
(deftest test-http-config
  (is (not (nil? (http-config)))))

(deftest test-db-config
  (is (not (nil? (db-config)))))