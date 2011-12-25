(ns conjure.core.util.test-conjure-utils
  (:use clojure.test
        conjure.core.util.conjure-utils))
        
(deftest test-conjure-namespaces
  (is (= #{"helpers.home-helper"} (conjure-namespaces 'controllers.home-controller))))