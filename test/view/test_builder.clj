(ns test.view.test-builder
  (:use test-helper
        clojure.contrib.test-is
        conjure.view.builder)
  (:require [conjure.view.util :as util]))

(def controller "test")
(def action "show")

(deftest test-find-or-create-controller-directory
  (let [controller-directory (find-or-create-controller-directory (util/find-views-directory) controller)]
    (test-directory controller-directory controller)
    (. controller-directory delete))
  (let [controller-directory (find-or-create-controller-directory controller)]
    (test-directory controller-directory controller)
    (. controller-directory delete))
  (is (nil? (find-or-create-controller-directory nil)))
  (is (nil? (find-or-create-controller-directory (util/find-views-directory) nil)))
  (is (nil? (find-or-create-controller-directory nil controller)))
  (is (nil? (find-or-create-controller-directory nil nil))))

(deftest test-create-view-file
  (let [view-file (create-view-file (find-or-create-controller-directory controller) action)]
    (test-file view-file (str action ".clj"))
    (. view-file delete))
  (is (nil? (create-view-file (find-or-create-controller-directory controller) nil)))
  (is (nil? (create-view-file nil action)))
  (is (nil? (create-view-file nil nil))))