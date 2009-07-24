(ns test.controller.test-builder
  (:use test-helper
        clojure.contrib.test-is
        conjure.controller.builder)
  (:require [conjure.controller.util :as util]))
        
(deftest test-create-controller-file
  (let [controller-directory (util/find-controllers-directory)
        controller-file (create-controller-file "test" controller-directory)]
    (test-file controller-file "test_controller.clj")
    (. controller-file delete))
  (let [controller-file (create-controller-file "test")]
    (test-file controller-file "test_controller.clj")
    (. controller-file delete))
  (is (nil? (create-controller-file (util/find-controllers-directory) nil)))
  (is (nil? (create-controller-file nil "test")))
  (is (nil? (create-controller-file nil nil)))
  (is (nil? (create-controller-file nil))))