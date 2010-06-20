(ns conjure.core.model.test-database-session-store
  (:use test-helper
        clojure.contrib.test-is
        conjure.core.model.database-session-store)
  (:require [conjure.core.config.environment :as environment]
            [conjure.core.model.database :as database]
            [conjure.core.server.request :as request]
            [conjure.core.util.session-utils :as session-utils]))

(def test-session-id "blah")

(defn init-database [test-fn]
  (environment/require-environment)
  (database/init-database)
  (test-fn))

(use-fixtures :once init-database)

(deftest test-session-store
  (is (= (:init session-store) init))
  (is (= (:drop session-store) drop-session))
  (is (= (:delete session-store) delete))
  (is (= (:save session-store) save))
  (is (= (:retrieve session-store) retrieve))
  
  (let [table-exists-at-init? (database/table-exists? session-table)]
    (init)
    (is (database/table-exists? session-table))
    
    (request/set-request-map { :params { :session-id test-session-id } }
      (create-session "foo" "bar")
      (is (= (retrieve) { "foo" "bar" }))
  
      (delete "foo")
      (is (= (retrieve) {})))
    
    (request/set-request-map { :request { :headers { "cookie" (str session-utils/session-id-name "=" test-session-id) } } }
      (save :foo "bar")
      (is (= (retrieve) { :foo "bar" }))
      
      (delete :foo)
      (is (= (retrieve) {})))
    
    (request/set-request-map { :temp-session test-session-id }
      (drop-session)
      (is (nil? (retrieve))))
    
    (database/drop-table session-table)
    (is (not (database/table-exists? session-table)))
    
    (if table-exists-at-init?
      (init))))