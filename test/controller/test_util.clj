(ns test.controller.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.controller.util)
  (:require [generators.controller-generator :as controller-generator]
            [destroyers.controller-destroyer :as controller-destroyer]))


(defn setup-all [function]
  (controller-generator/generate-controller-file "test" [])
  (function)
  (controller-destroyer/destroy-controller-file "test"))
        
(use-fixtures :once setup-all)
  
(deftest test-find-controllers-directory
  (let [controllers-directory (find-controllers-directory)]
    (is (not (nil? controllers-directory)))
    (is (instance? File controllers-directory))))