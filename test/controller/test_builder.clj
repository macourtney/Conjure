(ns test.controller.test-builder
  (:use test-helper
        clojure.contrib.test-is
        conjure.controller.builder)
  (:require [conjure.controller.util :as util]))
        
(deftest test-create-controller-file
  (let [controllers-directory (util/find-controllers-directory)
        controller-file (create-controller-file 
                          { :controller "test", 
                            :controllers-directory controllers-directory,
                            :silent true })]
    (test-file controller-file "test_controller.clj")
    (. controller-file delete))
  (let [controller-file (create-controller-file { :controller "test", :silent true })]
    (test-file controller-file "test_controller.clj")
    (. controller-file delete))
  (is (nil? (create-controller-file { :controller nil, :controllers-directory (util/find-controllers-directory), :silent true })))
  (is (nil? (create-controller-file { :controller "test", :controllers-directory nil, :silent true })))
  (is (nil? (create-controller-file { :controller nil, :controllers-directory nil, :silent true })))
  (is (nil? (create-controller-file { :controller nil, :silent true }))))