(ns conjure.core.util.test-execute-utils
  (:use clojure.contrib.test-is
        conjure.core.util.execute-utils))

(deftest test-run-script
  (run-script "noop" [])
  (run-script "fail" [])
  (run-script "invalid" []))