(ns test.test.test-builder
  (:use clojure.contrib.test-is
        conjure.test.builder)
  (:require [conjure.test.util :as util]
            [test-helper :as test-helper]))

(deftest test-find-or-create-functional-test-directory
  (is (nil? (util/find-functional-test-directory)))
  (test-helper/test-directory (find-or-create-functional-test-directory) "functional")
  (test-helper/test-directory (find-or-create-functional-test-directory) "functional")
  (is (. (util/find-functional-test-directory) delete)))

(defn test-functional-test [controller]
  (is (not (. (util/functional-test-file controller) exists)))
  (is (test-helper/test-file (create-functional-test controller) (util/functional-test-file-name controller)))
  (is (nil? (create-functional-test controller)))
  (is (. (util/functional-test-file controller) delete)))

(deftest test-create-functional-test
  (let [functional-test-directory (find-or-create-functional-test-directory)]
    (test-functional-test "test")
    (test-functional-test "foo-bar")
    (is (. functional-test-directory delete))))