(ns test.test.test-builder
  (:use clojure.contrib.test-is
        conjure.test.builder)
  (:require [conjure.test.util :as util]
            [conjure.util.file-utils :as file-utils]
            [test-helper :as test-helper]))

(deftest test-find-or-create-functional-test-directory
  (is (nil? (util/find-functional-test-directory)))
  (test-helper/test-directory (find-or-create-functional-test-directory) util/functional-dir-name)
  (test-helper/test-directory (find-or-create-functional-test-directory) util/functional-dir-name)
  (is (. (util/find-functional-test-directory) delete)))

(deftest test-find-or-create-unit-test-directory
  (is (nil? (util/find-unit-test-directory)))
  (test-helper/test-directory (find-or-create-unit-test-directory) util/unit-dir-name)
  (test-helper/test-directory (find-or-create-unit-test-directory) util/unit-dir-name)
  (is (. (util/find-unit-test-directory) delete)))

(deftest test-find-or-create-controller-view-unit-test-directory
  (let [controller "test"]
    (is (nil? (util/find-controller-view-unit-test-directory controller)))
    (test-helper/test-directory (find-or-create-controller-view-unit-test-directory controller) controller)
    (test-helper/test-directory (find-or-create-controller-view-unit-test-directory controller) controller)
    (is (. (util/find-controller-view-unit-test-directory controller) delete))
    (is (. (util/find-view-unit-test-directory) delete))
    (is (. (util/find-unit-test-directory) delete))))

(deftest test-find-or-create-model-unit-test-directory
  (is (nil? (util/find-model-unit-test-directory)))
  (test-helper/test-directory (find-or-create-model-unit-test-directory) util/unit-model-dir-name)
  (test-helper/test-directory (find-or-create-model-unit-test-directory) util/unit-model-dir-name)
  (is (file-utils/delete-all-if-empty (util/find-model-unit-test-directory) (util/find-unit-test-directory))))

(defn test-unit-test [controller action]
  (is (not (. (util/view-unit-test-file controller action) exists)))
  (is (test-helper/test-file (create-view-unit-test controller action) (util/view-unit-test-file-name action)))
  (is (nil? (create-view-unit-test controller action)))
  (is (. (util/view-unit-test-file controller action) delete)))

(deftest test-create-view-unit-test
  (let [controller "test"
        controller-test-directory (find-or-create-controller-view-unit-test-directory controller)]
    (test-unit-test controller "show")
    (test-unit-test controller "foo-bar")
    (is (file-utils/delete-all-if-empty controller-test-directory (util/find-view-unit-test-directory) (util/find-unit-test-directory)))))

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

(defn test-model-test [model]
  (let [model-unit-test (util/model-unit-test-file model)]
    (is (not (. model-unit-test exists)))
    (is (test-helper/test-file (create-model-unit-test model) (util/model-unit-test-file-name model)))
    (is (nil? (create-model-unit-test model)))
    (is (. model-unit-test delete))))

(deftest test-create-model-unit-test
  (let [model-test-directory (find-or-create-model-unit-test-directory)]
    (test-model-test "test")
    (test-model-test "foo-bar")
    (is (. model-test-directory delete))))