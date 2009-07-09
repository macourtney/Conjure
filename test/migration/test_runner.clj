(ns test.migration.test-runner
  (:use test-helper
        clojure.contrib.test-is
        conjure.migration.runner)
  (:require [conjure.migration.util :as util]
            [generators.migration-generator :as migration-generator]
            [destroyers.migration-destroyer :as migration-destroyer]))

(def first-migration "create-test")
(def second-migration "alter-test")

(defn setup-all [function]
  (migration-generator/generate-migration-file first-migration)
  (migration-generator/generate-migration-file second-migration)
  (function)
  (migration-destroyer/destroy-migration-file second-migration)
  (migration-destroyer/destroy-migration-file first-migration))
        
(use-fixtures :once setup-all)

(deftest test-current-db-version
  (is (= 0 (current-db-version))))
  
(deftest test-update-db-version
  (is (= 0 (current-db-version)))
  (update-db-version 1)
  (is (= 1 (current-db-version)))
  (update-db-version 0)
  (is (= 0 (current-db-version)))
  (update-db-version 0)
  (is (= 0 (current-db-version))))
  
(deftest test-run-migrate-up
  (is (= 0 (current-db-version)))
  (run-migrate-up (util/find-migration-file first-migration))
  (is (= 1 (current-db-version)))
  (update-db-version 0)
  (is (= 0 (current-db-version)))
  (run-migrate-up nil)
  (is (= 0 (current-db-version))))
  
(deftest test-run-migrate-down
  (is (= 0 (current-db-version)))
  (run-migrate-up (util/find-migration-file first-migration))
  (is (= 1 (current-db-version)))
  (run-migrate-down (util/find-migration-file first-migration))
  (is (= 0 (current-db-version)))
  (run-migrate-down nil)
  (is (= 0 (current-db-version)))
  (run-migrate-down first-migration)
  (is (= 0 (current-db-version))))
  
(deftest test-migrate-up-all
  (is (= 0 (current-db-version)))
  (migrate-up-all (util/all-migration-files))
  (is (= 2 (current-db-version)))
  (update-db-version 0)
  (is (= 0 (current-db-version)))
  (migrate-up-all)
  (is (= 2 (current-db-version)))
  (update-db-version 0)
  (is (= 0 (current-db-version)))
  (migrate-up-all nil)
  (is (= 0 (current-db-version))))
  
(deftest test-migrate-down-all
  (is (= 0 (current-db-version)))
  (migrate-up-all (util/all-migration-files))
  (is (= 2 (current-db-version)))
  (migrate-down-all (reverse (util/all-migration-files)))
  (is (= 0 (current-db-version)))
  (migrate-up-all)
  (is (= 2 (current-db-version)))
  (migrate-down-all)
  (is (= 0 (current-db-version)))
  (migrate-down-all nil)
  (is (= 0 (current-db-version))))
  
(deftest test-migrate-up
  (is (= 0 (current-db-version)))
  (migrate-up 0 1)
  (is (= 1 (current-db-version)))
  (update-db-version 0)
  (is (= 0 (current-db-version)))
  (migrate-up nil 1)
  (is (= 0 (current-db-version)))
  (migrate-up 0 nil)
  (is (= 0 (current-db-version)))
  (migrate-up nil nil)
  (is (= 0 (current-db-version))))
  
(deftest test-migrate-down
  (is (= 0 (current-db-version)))
  (migrate-up 0 1)
  (is (= 1 (current-db-version)))
  (migrate-down 1 0)
  (is (= 0 (current-db-version)))
  (migrate-down nil 0)
  (is (= 0 (current-db-version)))
  (migrate-down 1 nil)
  (is (= 0 (current-db-version)))
  (migrate-down nil nil)
  (is (= 0 (current-db-version))))
  
(deftest test-update-to-version
  (is (= 0 (current-db-version)))
  (update-to-version 1)
  (is (= 1 (current-db-version)))
  (update-to-version 2)
  (is (= 2 (current-db-version)))
  (update-to-version 3)
  (is (= 2 (current-db-version)))
  (update-to-version 1)
  (is (= 1 (current-db-version)))
  (update-to-version 0)
  (is (= 0 (current-db-version)))
  (update-to-version nil)
  (is (= 0 (current-db-version))))