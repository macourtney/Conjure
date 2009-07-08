(ns test.controller.test-base
  (:use clojure.contrib.test-is
        conjure.controller.base)
  (:require [generators.controller-generator :as controller-generator]
            [destroyers.controller-destroyer :as controller-destroyer]
            [destroyers.view-destroyer :as view-destroyer]))

(def controller-name "test")
(def action-name "show")

(defn setup-all [function]
  (controller-generator/generate-controller-file controller-name [action-name])
  (function)
  (controller-destroyer/destroy-controller-file controller-name)
  (view-destroyer/destroy-view-file controller-name action-name))
        
(use-fixtures :once setup-all)

(deftest test-render-view
  (let [view (render-view { :controller controller-name :action action-name })]
    (is (not (nil? view)))
    (is (instance? String view))
    (is (not (empty view)))))