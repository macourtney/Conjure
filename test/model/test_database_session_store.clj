(ns test.model.test-database-session-store
  (:use test-helper
        clojure.contrib.test-is
        conjure.model.database-session-store)
  (:require [conjure.model.database :as database]
            [conjure.util.session-utils :as session-utils]))

(def test-session-id "blah")

(deftest test-session-store
  (is (= (:init session-store) init))
  (is (= (:drop session-store) drop-session))
  (is (= (:delete session-store) delete))
  (is (= (:save session-store) save))
  (is (= (:retrieve session-store) retrieve))
  
  (let [table-exists-at-init? (database/table-exists? session-table)]
    (init)
    (is (database/table-exists? session-table))
    
    (let [request-map { :params { :session-id test-session-id } }]
      (create-session request-map "foo" "bar")
      (is (= (retrieve request-map) { "foo" "bar" }))
  
      (delete request-map "foo")
      (is (= (retrieve request-map) {})))
    
    (let [request-map { :headers { "cookie" (str session-utils/session-id-name "=" test-session-id) } }]
      (save request-map :foo "bar")
      (is (= (retrieve request-map) { :foo "bar" }))
      
      (delete request-map :foo)
      (is (= (retrieve request-map) {})))
    
    (let [request-map { :temp-session test-session-id }]
      (drop-session request-map)
      (is (nil? (retrieve request-map))))
    
    (database/drop-table session-table)
    (is (not (database/table-exists? session-table)))
    
    (if table-exists-at-init?
      (init))))