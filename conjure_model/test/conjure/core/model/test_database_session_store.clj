(ns conjure.core.model.test-database-session-store
  (:use test-helper
        clojure.test
        conjure.core.model.database-session-store)
  (:require [config.db-config :as db-config]
            [conjure.core.config.environment :as environment]
            [conjure.core.util.request :as request]
            [conjure.core.util.session-utils :as session-utils]
            [drift-db.core :as database]))

(def test-session-id "blah")

(use-fixtures :once init-server)

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