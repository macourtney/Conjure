(ns conjure.script.generators.test-flow-test-generator
  (:require [config.environments.test :as environments-test]) ;Loads the logger. 
  (:use clojure.test
        conjure.script.generators.flow-test-generator))

(def service "test")
(def actions ["show"])

(deftest test-generate-standard-content
  (is (generate-test-content service actions)))