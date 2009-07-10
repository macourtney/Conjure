(ns test.view.test-base
  (:use clojure.contrib.test-is
        conjure.view.base))

(defview [message]
  message)

(deftest test-defview
  (is (= "test" (render-view {} "test"))))