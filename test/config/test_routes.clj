(ns test.config.test-routes
  (:use clojure.contrib.test-is
        routes))

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

(deftest test-create-params
  (is (= { :id "1", :foo "bar" } (create-params { :params { :foo "bar" } } "1")))
  (is (= { :id "1" } (create-params {} "1")))
  (is (= { :id "1" } (create-params nil "1")))
  (is (= { :foo "bar" } (create-params { :params { :foo "bar" } } nil)))
  (is (= {} (create-params {} nil)))
  (is (= {} (create-params nil nil))))

(deftest test-call-controller
  (is (call-controller {} { :controller "home", :action "index" })))

(deftest test-route-request
  (is (route-request { :request { :uri "/home/index" } })))