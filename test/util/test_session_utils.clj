(ns test.util.test-session-utils
  (:use clojure.contrib.test-is
        conjure.util.session-utils)
  (:require [conjure.model.database :as database]
            [conjure.util.string-utils :as conjure-str-utils]))
  
(deftest test-create-session-id
  (is (create-session-id))
  (is (not (= (create-session-id) (create-session-id)))))
  
(deftest test-temp-session-id
  (is (= (temp-session-id { :temp-session "blah" }) "blah"))
  (is (nil? (temp-session-id { }))))

(deftest test-session-id
  (is (= (session-id { :params {:session-id "blah" } }) "blah"))
  (is (= (session-id { :headers { "cookie" (str session-id-name "=blah") } }) "blah"))
  (is (= (session-id { :temp-session "blah" }) "blah"))
  (is (nil? (session-id { :params { } })))
  (is (nil? (session-id { }))))

(deftest test-update-request-session
  (let [request-map (update-request-session {})]
    (is (contains? request-map :temp-session)))
  (let [request-map { :params { :session-id "blah" } }]
    (is (= request-map (update-request-session request-map))))
  (let [request-map { :headers { "cookie" (str session-id-name "=blah") } }]
    (is (= request-map (update-request-session request-map))))
  (let [request-map { :temp-session "blah" }]
    (is (= request-map (update-request-session request-map)))))
    
(deftest test-session-created?
  (is (not (session-created? {} {})))
  (is (not (session-created? { :temp-session "blah" } {})))
  (is (session-created? { :params { :session-id "blah" } } {}))
  (is (session-created? { :headers { "cookie" (str session-id-name "=blah") } } {}))
  (is (session-created? {} { :headers { "Set-Cookie" (str session-id-name "=blah") } })))
  
(deftest test-manage-session
  (let [response-map (manage-session {} {})]
    (is (:headers response-map))
    (is (get (:headers response-map) "Set-Cookie")))
  (let [response-map (manage-session { :headers { "cookie" "blah" } } {})]
    (is (nil? (:headers response-map))))
  (let [response-map (manage-session { :temp-session "blah" } {})]
    (is (= (get (conjure-str-utils/str-to-map (get (:headers response-map) "Set-Cookie")) session-id-name) "blah")))
  (let [response-map (manage-session {} { :headers { "Set-Cookie" (str session-id-name "=blah") } })]
    (is (:headers response-map))
    (is (= (get (:headers response-map) "Set-Cookie") (str session-id-name "=blah")))))