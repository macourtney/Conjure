(ns conjure.migration.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.migration.util)
  ;(:require [generators.migration-generator :as migration-generator]
  ;          [destroyers.migration-destroyer :as migration-destroyer])
  )
        
(def migration-name "create-test")
        
(defn setup-all [function]
  ;(migration-generator/generate-migration-file migration-name)
  (function)
  ;(migration-destroyer/destroy-all-dependencies migration-name)
  )
        
(use-fixtures :once setup-all)
        
(deftest test-find-db-directory
  (let [db-directory (find-db-directory)]
    (is (not (nil? db-directory)))
    (is (= "db" (. db-directory getName)))))
    
(deftest test-find-migrate-directory
  (let [migrate-directory (find-migrate-directory)]
    (is (not (nil? migrate-directory)))
    (is (= "migrate" (. migrate-directory getName)))))
    
(deftest test-all-migration-files
  (let [all-migrations (all-migration-files (find-migrate-directory))]
    (is (not (nil? all-migrations)))
    (is (seq? all-migrations))
    (is (not-empty all-migrations))
    (is (instance? File (first all-migrations))))
  (is (not (nil? (all-migration-files))))
  (is (nil? (all-migration-files nil))))
  
(deftest test-all-migration-file-names
  (let [all-migration-names (all-migration-file-names (find-migrate-directory))]
    (is (not (nil? all-migration-names)))
    (is (seq? all-migration-names))
    (is (not-empty all-migration-names))
    (is (instance? String (first all-migration-names))))
  (is (not (nil? (all-migration-file-names))))
  (is (nil? (all-migration-file-names nil))))
  
(deftest test-migration-number-from-file
  (let [migration-file (new File "001-create-test.clj")
        migration-number (migration-number-from-file migration-file)]
    (is (= migration-number 1)))
  (let [migration-file (new File "002-create-test.clj")
        migration-number (migration-number-from-file migration-file)]
    (is (= migration-number 2)))
  (let [migration-file (new File "000-create-test.clj")
        migration-number (migration-number-from-file migration-file)]
    (is (= migration-number 0)))
  (is (thrown? NumberFormatException (migration-number-from-file (new File "create-test.clj"))))
  (is (nil? (migration-number-from-file nil))))
  
(deftest test-migration-files-in-range
  (let [migration-files (migration-files-in-range 0 1)]
    (is (not-empty migration-files)))
  (let [migration-files (migration-files-in-range 0 0)]
    (is (empty migration-files))))
    
(deftest test-all-migration-numbers
  (let [migration-numbers (all-migration-numbers (find-migrate-directory))]
    (is (not-empty migration-numbers))
    (is (number? (first migration-numbers))))
  (let [migration-numbers (all-migration-numbers)]
    (is (not-empty migration-numbers))
    (is (number? (first migration-numbers))))
  (is (nil? (all-migration-numbers nil))))
  
(deftest test-max-migration-number
  (let [max-number (max-migration-number (find-migrate-directory))]
    (is (= max-number 1)))
  (let [max-number (max-migration-number)]
    (is (= max-number 1)))
  (is (nil? (max-migration-number nil))))
  
(deftest test-find-next-migrate-number
  (let [next-number (find-next-migrate-number (find-migrate-directory))]
    (is (= next-number 2)))
  (let [next-number (find-next-migrate-number)]
    (is (= next-number 2)))
  (is (nil? (find-next-migrate-number nil))))
  
(deftest test-find-migration-file
  (let [migration-file (find-migration-file (find-migrate-directory) migration-name)]
    (is (not (nil? migration-file)))
    (is (instance? File migration-file)))
  (let [migration-file (find-migration-file migration-name)]
    (is (not (nil? migration-file)))
    (is (instance? File migration-file)))
  (is (nil? (find-migration-file nil))))
  
(deftest test-migration-namespace
  (let [migration-ns (migration-namespace (find-migration-file migration-name))]
    (is (not (nil? migration-ns)))
    (is (= (str "migrate.001-" migration-name) migration-ns)))
  (is (nil? (migration-namespace nil))))
  
(deftest test-migration-number-before
  (let [migration-number (migration-number-before 1 (all-migration-files))]
    (is (= 0 migration-number)))
  (let [migration-number (migration-number-before 1)]
    (is (= 0 migration-number)))
  (is (nil? (migration-number-before nil)))
  (is (nil? (migration-number-before nil (all-migration-files)))))