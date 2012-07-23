(ns conjure.script.generators.test-flow-generator
  (:require [config.environments.test :as environments-test]) ;Loads the logger. 
  (:use clojure.test
        conjure.script.generators.flow-generator))

(def service "test")
(def actions ["show"])

(deftest test-generate-standard-content
  (is (generate-flow-content service (generate-all-action-functions actions))))