(ns test.model.test-session-store
  (:use test-helper
        clojure.contrib.test-is
        conjure.model.session-store)
  (:require [conjure.util.session-utils :as session-utils]))

(def test-session-id "blah")

(deftest test-session-store
  (let [request-map { :headers { "cookie" (str session-utils/session-id-name "=" test-session-id) } }]
    (save request-map :foo "bar")
    (is (= (retrieve request-map) { :foo "bar" }))
    (is (= (retrieve-value request-map :foo) "bar")))
  
  (let [request-map { :params { :session-id test-session-id } }]
    (delete request-map :foo)
    (is (= (retrieve request-map) {})))
  
  (let [request-map { :temp-session test-session-id }]
    (drop-session request-map)
    (is (nil? (retrieve request-map)))))