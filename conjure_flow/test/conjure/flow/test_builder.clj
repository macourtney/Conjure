(ns conjure.flow.test-builder
  (:use clojure.test
        conjure.flow.builder
        test-helper)
  (:require [conjure.flow.util :as util]))

(use-fixtures :once init-server)

(deftest test-create-flow-file
  (let [flows-directory (util/find-flows-directory)
        flow-file (create-flow-file 
                          { :service "builder-test", 
                            :flows-directory flows-directory,
                            :silent true })]
    (test-file flow-file "builder_test_flow.clj")
    (when flow-file
      (.delete flow-file)))
  (let [flow-file (create-flow-file { :service "builder-test", :silent true })]
    (test-file flow-file "builder_test_flow.clj")
    (when flow-file
      (.delete flow-file)))
  (is (nil? (create-flow-file { :service nil, :flows-directory (util/find-flows-directory), :silent true })))
  (is (nil? (create-flow-file { :service "test", :flows-directory nil, :silent true })))
  (is (nil? (create-flow-file { :service nil, :flows-directory nil, :silent true })))
  (is (nil? (create-flow-file { :service nil, :silent true }))))