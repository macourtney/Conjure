(ns conjure.core.config.test-routes-util
  (:use clojure.contrib.test-is
        conjure.core.config.routes-util
        test-helper)
  (:require [clout.core :as clout]
            [conjure.core.server.request :as request]
            [config.routes :as routes]))

(use-fixtures :once init-server)

(defn
  fail-route-function []
  nil)

(defn
  test-route-function []
  :route-function)

(deftest test-function-parse
  (binding [routes/routes { :functions [test-route-function] }]
    (is (= :route-function (function-parse))))
  (binding [routes/routes { :functions [fail-route-function test-route-function] }]
    (is (= :route-function (function-parse))))
  (binding [routes/routes { :functions [test-route-function fail-route-function] }]
    (is (= :route-function (function-parse))))
  (binding [routes/routes { :functions [] }]
    (is (nil? (function-parse))))
  (binding [routes/routes {}]
    (is (nil? (function-parse)))))

(deftest test-symbol-replace
  (is (= 
    { :controller "home" } 
    (symbol-replace { :controller 'controller } { "controller" "home" })))
  (is (= 
    { :params { :id "1"} } 
    (symbol-replace { :params { :id 'id } } { "id" "1" })))
  (is (= 
    { :controller "home" :action "index" } 
    (symbol-replace { :controller 'controller :action "index" } { "controller" "home" })))
  (is (= 
    { :controller nil } 
    (symbol-replace { :controller 'controller } {}))))

(deftest test-parse-compiled-route
  (is (= 
    { :controller "home" }
    (parse-compiled-route 
      { :route (clout/route-compile "/:controller")
        :request-map { :controller 'controller } }
      "/home")))
  (is (nil? 
    (parse-compiled-route 
      { :route (clout/route-compile "/:controller")
        :request-map { :controller 'controller } }
      "/home/index")))
  (is (= 
    { :controller "home", :action "index" }
    (parse-compiled-route 
      { :route (clout/route-compile "/:controller/:action")
        :request-map { :controller 'controller, :action 'action } }
      "/home/index")))
  (is (= 
    { :controller "home", :action "index", :params { :id "1" } }
    (parse-compiled-route 
      { :route (clout/route-compile "/:controller/:action/:id")
        :request-map { :controller 'controller, :action 'action, :params { :id 'id } } }
      "/home/index/1")))
  (is (= 
    { :controller "home", :action "index" }
    (parse-compiled-route 
      { :route (clout/route-compile "/")
        :request-map { :controller "home", :action "index" } }
      "/")))
  (is (= 
    { :controller "message", :action "list-records" }
    (parse-compiled-route 
      { :route (clout/route-compile "/:controller/:action")
        :request-map { :controller 'controller, :action 'action } }
      "/message/list_records"))))

(deftest test-compiled-parse
  (request/set-request-map { :request { :uri "/home/index/1" } }
    (is (= 
      { :controller "home", :action "index", :params { :id "1" } }
      (compiled-parse))))
  (request/set-request-map { :request { :uri "/home/index" } }
    (is (= 
      { :controller "home", :action "index" } 
      (compiled-parse))))
  (request/set-request-map { :request { :uri "/home" } }
    (is (= 
      { :controller "home", :action "index" }
      (compiled-parse))))
  (request/set-request-map { :request { :uri "/" } }
    (is (= 
      { :controller "home", :action "index" }
      (compiled-parse))))
  (request/set-request-map { :request { :uri "/home/index/1/fail" } }
    (is (nil? (compiled-parse)))))

(deftest test-parse-path
  (request/set-request-map { :request { :uri "/home/index/1" } }
    (is (= 
      { :controller "home", :action "index", :params { :id "1" } }
      (parse-path))))
  (request/set-request-map { :request { :uri "/home/index/1" } }
    (binding [routes/routes { :functions [test-route-function] }]
      (is (= :route-function (parse-path))))))

(deftest test-call-controller
  (is (call-controller { :controller "home", :action "index" }))
  (request/set-request-map { :controller "home", :action "index" }
    (is (call-controller {}))))

(deftest test-route-request
  (request/set-request-map { :request { :uri "/home/index" } }
    (is (route-request))))