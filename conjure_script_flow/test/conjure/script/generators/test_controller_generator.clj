(ns conjure.script.generators.test-controller-generator
  (:require [config.environments.test :as environments-test]) ;Loads the logger. 
  (:use clojure.test
        conjure.script.generators.controller-generator))

(def controller "test")
(def actions ["show"])

(deftest test-generate-standard-content
  (is (generate-controller-content controller (generate-all-action-functions actions))))