(ns conjure.script.generators.test-xml-view-generator
  (:use clojure.test
        conjure.script.generators.xml-view-generator)
  (:require [conjure.core.view.util :as view-util]))

(deftest test-generate-standard-content
  (is (generate-standard-content "views.test.show" "[:test]")))