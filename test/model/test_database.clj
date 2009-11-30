(ns test.model.test-database
  (:use test-helper
        clojure.contrib.test-is
        conjure.model.database)
  (:require [conjure.model.util :as util]))

(def test-table "test_table")

(deftest test-db-flavor
  (let [flavor (db-flavor)]
    (is (not (nil? flavor)))
    (is (map? flavor)))
  (let [db-map (db-flavor :db-map)]
    (is (not (nil? db-map)))
    (is (fn? db-map))))
    
(deftest test-table-creation
  (is (not (table-exists? test-table)))
  (create-table test-table 
    (id)
    (string "value"))
  (is (table-exists? test-table))
  (insert-into test-table { :id 1 :value "test-value" })
  (let [test-row (first (sql-find { :table test-table :where "id = 1" }))]
    (println "test-row:" test-row)
    (is (not (nil? test-row)))
    (is (map? test-row))
    (is (= "test-value" (:value test-row))))
  (update test-table ["id = ?" 1] { :value "test-value2" })
  (let [test-row (first (sql-find { :table test-table :where "id = 1" }))]
    (is (not (nil? test-row)))
    (is (map? test-row))
    (is (= "test-value2" (:value test-row))))
  (drop-table test-table)
  (is (not (table-exists? test-table))))
  
(deftest test-integer
  (is (not (nil? (integer "test-int")))))
  
(deftest test-string
  (is (not (nil? (string "test-string")))))

(deftest test-text
  (is (not (nil? (text "test-text")))))

(deftest test-belongs-to
  (is (not (nil? (belongs-to "test-belongs-to")))))

(deftest test-id
  (is (not (nil? (id)))))

(deftest test-date
  (is (not (nil? (date "test-date")))))
  
(deftest test-time-type
  (is (not (nil? (time-type "test-time")))))
  
(deftest test-date-time
  (is (not (nil? (date-time "test-date-time")))))