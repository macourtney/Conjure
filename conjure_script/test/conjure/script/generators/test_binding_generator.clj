(ns conjure.script.generators.test-binding-generator
  (:use clojure.test
        conjure.script.generators.binding-generator))

(def controller "test")
(def action "show") 

(deftest test-generate-binding-function
  (is (generate-binding-function)))

(deftest test-generate-binding-content
  (is (generate-binding-content controller action (generate-binding-function))))