(ns leiningen.conjure_test
  (:use [leiningen.conjure] :reload-all)
  (:use [clojure.test]))

(deftest test-conjure
  (conjure {} "noop"))