(ns test.util.test-string-utils
  (:use clojure.contrib.test-is
        conjure.util.string-utils))

(deftest test-str-keyword
   (is (= (str-keyword :test) "test"))
   (is (= (str-keyword "test") "test")))