(ns conjure.script.generators.test-controller-test-generator
  (:require [config.environments.test :as environments-test]) ;Loads the logger. 
  (:use clojure.test
        conjure.script.generators.controller-test-generator))

(def controller "test")
(def actions ["show"])

(deftest test-generate-standard-content
  (is (generate-test-content controller actions)))