(ns conjure.util.test-execute-utils
  (:use clojure.test
        conjure.util.execute-utils))

(deftest test-run-script
  (run-script "noop" [])
  (run-script "fail" [])
  (run-script "invalid" []))