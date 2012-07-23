(ns conjure.script.generators.test-view-generator
  (:use clojure.test
        conjure.script.generators.view-generator)
  (:require [conjure.view.util :as view-util]))

(deftest test-generate-standard-content
  (is (generate-standard-content "views.test.show" "[:test]")))