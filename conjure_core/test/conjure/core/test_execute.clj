(ns conjure.core.test-execute
  (:use clojure.contrib.test-is
        conjure.core.execute))

(deftest test--main
  (-main "noop"))