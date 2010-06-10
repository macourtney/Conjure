(ns test.config.test-routes
  (:use clojure.contrib.test-is
        routes)
  (:require [conjure.server.request :as request]))

(def controller-name "test")
(def action-name "show")
(def id "1")

(deftest test-parse-path
  (is (=
    { :action action-name, :controller controller-name, :id id }
    (parse-path (str "/" controller-name "/" action-name "/" id))))
  (is (=
    { :action action-name, :controller controller-name }
    (parse-path (str "/" controller-name "/" action-name))))
  (is (=
    { :action action-name, :controller controller-name }
    (parse-path (str "/" controller-name "/" action-name "/"))))
  (is (=
    { :action "index", :controller controller-name }
    (parse-path (str "/" controller-name))))
  (is (=
    { :action "index", :controller controller-name }
    (parse-path (str "/" controller-name "/"))))
  (is (=
    { :action "index", :controller "home" }
    (parse-path "/"))))

(deftest test-call-controller
  (is (call-controller { :controller "home", :action "index" })))

(deftest test-route-request
  (request/set-request-map { :request { :uri "/home/index" } }
    (is (route-request))))