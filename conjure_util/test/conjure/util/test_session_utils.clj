(ns conjure.util.test-session-utils
  (:use clojure.test
        conjure.util.session-utils)
  (:require [conjure.util.request :as request]
            [clojure.tools.string-utils :as conjure-str-utils]))
  
(deftest test-create-session-id
  (is (create-session-id))
  (is (not (= (create-session-id) (create-session-id)))))
  
(deftest test-temp-session-id
  (request/set-request-map { :temp-session "blah" }
    (is (= (temp-session-id) "blah")))
  (is (nil? (temp-session-id))))

(deftest test-session-id
  (request/set-request-map { :params {:session-id "blah" } }
    (is (= (session-id) "blah")))
  (request/set-request-map { :request { :headers { "cookie" (str session-id-name "=blah") } } }
    (is (= (session-id) "blah")))
  (request/set-request-map { :temp-session "blah" }
    (is (= (session-id) "blah")))
  (request/set-request-map { :params { } }
    (is (nil? (session-id))))
  (is (nil? (session-id))))

(deftest test-update-request-session
  (let [request-map (update-request-session {})]
    (is (contains? request-map :temp-session)))
  (let [request-map { :params { :session-id "blah" } }]
    (request/set-request-map request-map
      (is (= request-map (update-request-session request-map)))))
  (let [request-map { :request { :headers { "cookie" (str session-id-name "=blah") } } }]
    (is (= request-map (update-request-session request-map))))
  (let [request-map { :temp-session "blah" }]
    (is (= request-map (update-request-session request-map)))))
    
(deftest test-session-created?
  (is (not (session-created? {})))
  (request/set-request-map { :temp-session "blah" }
    (is (not (session-created? {}))))
  (request/set-request-map { :params { :session-id "blah" } }
    (is (session-created? {})))
  (request/set-request-map { :request { :headers { "cookie" (str session-id-name "=blah") } } }
    (is (session-created? {})))
  (is (session-created? { :headers { "Set-Cookie" (str session-id-name "=blah") } })))
  
(deftest test-manage-session
  (let [response-map (manage-session {})]
    (is (:headers response-map))
    (is (get (:headers response-map) "Set-Cookie")))
  (request/set-request-map { :request { :headers { "cookie" "blah" } } }
    (let [response-map (manage-session {})]
      (is (nil? (:headers response-map)))))
  (request/set-request-map { :temp-session "blah" }
    (let [response-map (manage-session {})]
      (is (= (get (conjure-str-utils/str-to-map (get (:headers response-map) "Set-Cookie")) session-id-name) "blah"))))
  (let [response-map (manage-session { :headers { "Set-Cookie" (str session-id-name "=blah") } })]
    (is (:headers response-map))
    (is (= (get (:headers response-map) "Set-Cookie") (str session-id-name "=blah")))))