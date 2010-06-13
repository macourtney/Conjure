(ns conjure.controller.test-builder
  (:use clojure.contrib.test-is
        conjure.controller.builder
        test-helper)
  (:require [conjure.controller.util :as util]))

(use-fixtures :once init-server)

(deftest test-create-controller-file
  (let [controllers-directory (util/find-controllers-directory)
        controller-file (create-controller-file 
                          { :controller "builder-test", 
                            :controllers-directory controllers-directory,
                            :silent true })]
    (test-file controller-file "builder_test_controller.clj")
    (when controller-file
      (.delete controller-file)))
  (let [controller-file (create-controller-file { :controller "builder-test", :silent true })]
    (test-file controller-file "builder_test_controller.clj")
    (when controller-file
      (.delete controller-file)))
  (is (nil? (create-controller-file { :controller nil, :controllers-directory (util/find-controllers-directory), :silent true })))
  (is (nil? (create-controller-file { :controller "test", :controllers-directory nil, :silent true })))
  (is (nil? (create-controller-file { :controller nil, :controllers-directory nil, :silent true })))
  (is (nil? (create-controller-file { :controller nil, :silent true }))))