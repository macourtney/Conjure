(ns test.test.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.test.util)
  (:require [conjure.test.builder :as builder]
            [test-helper :as test-helper]))

(deftest test-find-test-directory
  (let [test-directory (find-test-directory)]
    (test-helper/test-directory test-directory test-dir-name)))

(defn delete-dir [dir]
  (println "Deleting dir:" (. dir getName))
  (is (. dir delete)))
  
(defn delete-dirs [& dirs]
  (doall (map #(delete-dir %) dirs)))

(defn
  find-directory-test [dir-name find-function parent-directory-function create-directory-function dirs-to-delete]
  (println "dirs-to-delete:" dirs-to-delete)
  (create-directory-function)
  (let [found-directory (find-function (parent-directory-function))]
    (test-helper/test-directory found-directory dir-name)
    (is (. found-directory delete))
    (doall (map #(delete-dir (%)) dirs-to-delete)))
  (create-directory-function)
  (let [found-directory (find-function)]
    (test-helper/test-directory found-directory dir-name)
    (is (. found-directory delete))
    (doall (map #(delete-dir (%)) dirs-to-delete))))

(deftest test-find-functional-test-directory
  (find-directory-test
    functional-dir-name 
    find-functional-test-directory 
    find-test-directory 
    builder/find-or-create-functional-test-directory
    []))

(deftest test-find-unit-test-directory
  (find-directory-test 
    unit-dir-name 
    find-unit-test-directory 
    find-test-directory 
    builder/find-or-create-unit-test-directory
    []))

(deftest test-find-view-unit-test-directory
  (find-directory-test 
    unit-view-dir-name 
    find-view-unit-test-directory 
    find-unit-test-directory
    builder/find-or-create-view-unit-test-directory
    [find-unit-test-directory]))

(deftest test-find-controller-view-unit-test-directory
  (let [controller "test"]
    (find-directory-test 
      controller 
      (partial find-controller-view-unit-test-directory controller) 
      find-view-unit-test-directory 
      (partial builder/find-or-create-controller-view-unit-test-directory controller)
      [find-view-unit-test-directory find-unit-test-directory])))

(deftest test-functional-test-file-name
  (is (= (functional-test-file-name "test") "test_controller_test.clj"))
  (is (= (functional-test-file-name "foo-bar") "foo_bar_controller_test.clj"))
  (is (= (functional-test-file-name "foo_bar") "foo_bar_controller_test.clj"))
  (is (nil? (functional-test-file-name nil)))
  (is (nil? (functional-test-file-name ""))))

(deftest test-view-unit-test-file-name
  (is (= (view-unit-test-file-name "test") "test_view_test.clj"))
  (is (= (view-unit-test-file-name "foo-bar") "foo_bar_view_test.clj"))
  (is (= (view-unit-test-file-name "foo_bar") "foo_bar_view_test.clj"))
  (is (nil? (view-unit-test-file-name nil)))
  (is (nil? (view-unit-test-file-name ""))))

(deftest test-functional-test-file
  (builder/find-or-create-functional-test-directory)
  (test-helper/test-file (functional-test-file "test") "test_controller_test.clj")
  (test-helper/test-file (functional-test-file "foo-bar" (find-functional-test-directory)) "foo_bar_controller_test.clj")
  (is (nil? (functional-test-file nil)))
  (is (nil? (functional-test-file "")))
  (delete-dirs (find-functional-test-directory)))

(deftest test-view-unit-test-file
  (let [controller "test"]
    (builder/find-or-create-controller-view-unit-test-directory controller)
    (test-helper/test-file (view-unit-test-file controller "show") "show_view_test.clj")
    (test-helper/test-file (view-unit-test-file controller "show-baz" (find-controller-view-unit-test-directory controller)) "show_baz_view_test.clj")
    (is (nil? (view-unit-test-file controller nil)))
    (is (nil? (view-unit-test-file nil "show")))
    (is (nil? (view-unit-test-file controller "")))
    (is (nil? (view-unit-test-file "" "show")))
    (delete-dirs (find-controller-view-unit-test-directory controller) (find-view-unit-test-directory) (find-unit-test-directory))))

(deftest test-functional-test-namespace
  (is (= (functional-test-namespace "test") "test.functional.test-controller-test"))
  (is (= (functional-test-namespace "foo-bar") "test.functional.foo-bar-controller-test"))
  (is (= (functional-test-namespace "foo_bar") "test.functional.foo-bar-controller-test"))
  (is (nil? (functional-test-namespace nil)))
  (is (nil? (functional-test-namespace ""))))

(deftest test-view-unit-test-namespace
  (is (= (view-unit-test-namespace "test" "show") "test.unit.view.test.show-view-test"))
  (is (= (view-unit-test-namespace "foo-bar" "show-biz") "test.unit.view.foo-bar.show-biz-view-test"))
  (is (= (view-unit-test-namespace "foo_bar" "show_biz") "test.unit.view.foo-bar.show-biz-view-test"))
  (is (nil? (view-unit-test-namespace "test" nil)))
  (is (nil? (view-unit-test-namespace nil "show")))
  (is (nil? (view-unit-test-namespace "test" "")))
  (is (nil? (view-unit-test-namespace "" "show"))))