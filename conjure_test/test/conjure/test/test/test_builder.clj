(ns conjure.test.test.test-builder
  (:use clojure.test
        conjure.test.builder))

(deftest test-find-or-create-functional-test-directory
  (find-or-create-functional-test-directory { :silent true }))