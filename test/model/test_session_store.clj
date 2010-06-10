(ns test.model.test-session-store
  (:use test-helper
        clojure.contrib.test-is
        conjure.model.session-store)
  (:require [conjure.util.session-utils :as session-utils]
            [conjure.server.request :as request]))

(def test-session-id "blah")

(deftest test-session-store
  (request/set-request-map 
    { :request { :headers { "cookie" (str session-utils/session-id-name "=" test-session-id) } } }
    (save :foo "bar")
    (is (= (retrieve) { :foo "bar" }))
    (is (= (retrieve-value :foo) "bar")))
  
  (request/set-request-map { :params { :session-id test-session-id } }
    (delete :foo)
    (is (= (retrieve) {})))
  
  (request/set-request-map { :temp-session test-session-id }
    (drop-session)
    (is (nil? (retrieve)))))