(ns conjure.migration.test-builder
  (:use test-helper
        clojure.contrib.test-is
        conjure.migration.builder)
  (:require [conjure.migration.util :as util]))

(use-fixtures :once init-server)

(deftest test-find-or-create-migrate-directory
  (let [db-directory (util/find-db-directory)
        migrate-directory (find-or-create-migrate-directory db-directory)]
    (test-directory migrate-directory "migrate"))
  (test-directory (find-or-create-migrate-directory) "migrate")
  (is (nil? (find-or-create-migrate-directory nil))))
  
(deftest test-create-migration-file
  (let [migrate-directory (find-or-create-migrate-directory)
        migration-file (create-migration-file migrate-directory "builder-test")]
    (test-file migration-file "003_builder_test.clj")
    (.delete migration-file))
  (let [migration-file (create-migration-file "builder-test")]
    (test-file migration-file "003_builder_test.clj")
    (.delete migration-file))
  (is (nil? (create-migration-file nil)))
  (is (nil? (create-migration-file (find-or-create-migrate-directory) nil)))
  (is (nil? (create-migration-file nil "create-test")))
  (is (nil? (create-migration-file nil nil))))