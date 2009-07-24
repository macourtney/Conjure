(ns test.test.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.test.util)
  (:require [conjure.test.builder :as builder]
            [test-helper :as test-helper]))

(deftest test-find-test-directory
  (let [test-directory (find-test-directory)]
    (test-helper/test-directory test-directory "test")))

(deftest test-find-functional-test-directory
  (builder/find-or-create-functional-test-directory)
  (let [functional-test-directory (find-functional-test-directory (find-test-directory))]
    (test-helper/test-directory functional-test-directory "functional")
    (. functional-test-directory delete))
  (builder/find-or-create-functional-test-directory)
  (let [functional-test-directory (find-functional-test-directory)]
    (test-helper/test-directory functional-test-directory "functional")
    (. functional-test-directory delete)))

(deftest test-functional-test-file-name
  (is (= (functional-test-file-name "test") "test_controller_test.clj"))
  (is (= (functional-test-file-name "foo-bar") "foo_bar_controller_test.clj"))
  (is (= (functional-test-file-name "foo_bar") "foo_bar_controller_test.clj"))
  (is (nil? (functional-test-file-name nil)))
  (is (nil? (functional-test-file-name ""))))

(deftest test-functional-test-namespace
  (is (= (functional-test-namespace "test") "test.functional.test-controller-test"))
  (is (= (functional-test-namespace "foo-bar") "test.functional.foo-bar-controller-test"))
  (is (= (functional-test-namespace "foo_bar") "test.functional.foo-bar-controller-test"))
  (is (nil? (functional-test-namespace nil)))
  (is (nil? (functional-test-namespace ""))))