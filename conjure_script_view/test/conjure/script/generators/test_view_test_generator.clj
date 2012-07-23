(ns conjure.script.generators.test-view-test-generator
  (:use clojure.test
        conjure.script.generators.view-test-generator)
  (:require [conjure.view.util :as view-util]))

(deftest test-generate-standard-content
  (is (generate-test-content "test" "show")))