(ns conjure.test.test.test-util
  (:use clojure.test
        conjure.test.util))

(deftest test-functional-test-file-name
  (let [service "test"]
    (is (= (functional-test-file-name service) (str service "_flow_test.clj")))))