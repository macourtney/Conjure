(ns conjure.core.model.test-memory-session-store
  (:use test-helper
        clojure.contrib.test-is
        conjure.core.model.memory-session-store)
  (:require [conjure.core.server.request :as request]
            [conjure.core.util.session-utils :as session-utils]))

(def test-session-id "blah")

(deftest test-session-store
  (is (= (:init session-store) init))
  (is (= (:drop session-store) drop-session))
  (is (= (:delete session-store) delete))
  (is (= (:save session-store) save))
  (is (= (:retrieve session-store) retrieve))
  
  (is (= {} @data-store))
  
  (init)
  (is (= {} @data-store))
  
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
  
  (is (= {} @data-store)))