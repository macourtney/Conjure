(ns conjure.core.migration.test-util
  (:use test-helper
        clojure.contrib.test-is
        conjure.core.migration.util))

(def first-migration "create-tests")
(def second-migration "tests-update")

(defn reset-db [test-fn]
  (init-server test-fn)
  (update-version 0))

(use-fixtures :once reset-db)

(deftest test-current-version
  (is (= 0 (current-version))))
  
(deftest test-update-version
  (is (= 0 (current-version)))
  (update-version 1)
  (is (= 1 (current-version)))
  (update-version 0)
  (is (= 0 (current-version)))
  (update-version 0)
  (is (= 0 (current-version))))