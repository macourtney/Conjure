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
  (migration-generator/generate-migration-file first-migration "" "")
  (migration-generator/generate-migration-file second-migration "" "")
  (function)
  (migration-destroyer/destroy-all-dependencies second-migration)
  (migration-destroyer/destroy-all-dependencies first-migration))
        
(use-fixtures :once setup-all)

(deftest test-current-db-version
  (is (= 0 (current-db-version))))
  
(deftest test-update-db-version
  (is (= 0 (current-db-version true)))
  (update-db-version 1)
  (is (= 1 (current-db-version true)))
  (update-db-version 0)
  (is (= 0 (current-db-version true)))
  (update-db-version 0)
  (is (= 0 (current-db-version true))))
  
(deftest test-run-migrate-up
  (is (= 0 (current-db-version true)))
  (run-migrate-up (util/find-migration-file first-migration))
  (is (= 1 (current-db-version true)))
  (update-db-version 0)
  (is (= 0 (current-db-version true)))
  (run-migrate-up nil)
  (is (= 0 (current-db-version true))))
  
(deftest test-run-migrate-down
  (is (= 0 (current-db-version true)))
  (run-migrate-up (util/find-migration-file first-migration))
  (is (= 1 (current-db-version true)))
  (run-migrate-down (util/find-migration-file first-migration))
  (is (= 0 (current-db-version true)))
  (run-migrate-down nil)
  (is (= 0 (current-db-version true)))
  (run-migrate-down first-migration)
  (is (= 0 (current-db-version true))))
  
(deftest test-migrate-up-all
  (is (= 0 (current-db-version true)))
  (migrate-up-all (util/all-migration-files))
  (is (= 2 (current-db-version true)))
  (update-db-version 0)
  (is (= 0 (current-db-version true)))
  (migrate-up-all)
  (is (= 2 (current-db-version true)))
  (update-db-version 0)
  (is (= 0 (current-db-version true)))
  (migrate-up-all nil true)
  (is (= 0 (current-db-version true))))
  
(deftest test-migrate-down-all
  (is (= 0 (current-db-version true)))
  (migrate-up-all (util/all-migration-files) true)
  (is (= 2 (current-db-version true)))
  (migrate-down-all (reverse (util/all-migration-files)))
  (is (= 0 (current-db-version true)))
  (migrate-up-all (util/all-migration-files) true)
  (is (= 2 (current-db-version true)))
  (migrate-down-all)
  (is (= 0 (current-db-version true)))
  (migrate-down-all nil true)
  (is (= 0 (current-db-version true))))
  
(deftest test-migrate-up
  (is (= 0 (current-db-version true)))
  (migrate-up 0 1 true)
  (is (= 1 (current-db-version true)))
  (update-db-version 0)
  (is (= 0 (current-db-version true)))
  (migrate-up nil 1 true)
  (is (= 0 (current-db-version true)))
  (migrate-up 0 nil true)
  (is (= 0 (current-db-version true)))
  (migrate-up nil nil true)
  (is (= 0 (current-db-version true))))
  
(deftest test-migrate-down
  (is (= 0 (current-db-version true)))
  (migrate-up 0 1 true)
  (is (= 1 (current-db-version true)))
  (migrate-down 1 0)
  (is (= 0 (current-db-version true)))
  (migrate-down nil 0 true)
  (is (= 0 (current-db-version true)))
  (migrate-down 1 nil true)
  (is (= 0 (current-db-version true)))
  (migrate-down nil nil true)
  (is (= 0 (current-db-version true))))
  
(deftest test-update-to-version
  (is (= 0 (current-db-version true)))
  (update-to-version 1)
  (is (= 1 (current-db-version true)))
  (update-to-version 2 true)
  (is (= 2 (current-db-version true)))
  (update-to-version 3 true)
  (is (= 2 (current-db-version true)))
  (update-to-version 1 true)
  (is (= 1 (current-db-version true)))
  (update-to-version 0 true)
  (is (= 0 (current-db-version true)))
  (update-to-version nil true)
  (is (= 0 (current-db-version true))))