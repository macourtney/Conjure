(ns conjure.config.test-routes-util
  (:use clojure.test
        conjure.config.routes-util
        test-helper)
  (:require [clout.core :as clout]
            [conjure.util.request :as request]
            [config.routes :as routes]))

;(use-fixtures :once init-server)

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
    { :service "home" } 
    (symbol-replace { :service 'service } { :service "home" })))
  (is (= 
    { :service "home" } 
    (symbol-replace { :service 'service } { "service" "home" })))
  (is (= 
    { :params { :id "1"} } 
    (symbol-replace { :params { :id 'id } } { :id "1" })))
  (is (= 
    { :service "home" :action "index" } 
    (symbol-replace { :service 'service :action "index" } { :service "home" })))
  (is (= 
    { :service nil } 
    (symbol-replace { :service 'service } {}))))

(deftest test-parse-compiled-route
  (is (= 
    { :service "home" }
    (parse-compiled-route 
      { :route (clout/route-compile "/:service")
        :request-map { :service 'service } }
      { :uri "/home" })))
  (is (nil? 
    (parse-compiled-route 
      { :route (clout/route-compile "/:service")
        :request-map { :service 'service } }
      { :uri "/home/index" })))
  (is (= 
    { :service "home", :action "index" }
    (parse-compiled-route 
      { :route (clout/route-compile "/:service/:action")
        :request-map { :service 'service, :action 'action } }
      { :uri "/home/index" })))
  (is (= 
    { :service "home", :action "index", :params { :id "1" } }
    (parse-compiled-route 
      { :route (clout/route-compile "/:service/:action/:id")
        :request-map { :service 'service, :action 'action, :params { :id 'id } } }
      { :uri "/home/index/1" })))
  (is (= 
    { :service "home", :action "index", :params { :id "1" } }
    (parse-compiled-route 
      { :route (clout/route-compile "/:service/:action/:id")
        :request-map { :service 'service, :action 'action, :params { :id 'id } } }
      { :uri "/home/index/1"
        :query-string "foo=bar&baz=biz" })))
  (is (= 
    { :service "home", :action "index" }
    (parse-compiled-route 
      { :route (clout/route-compile "/")
        :request-map { :service "home", :action "index" } }
      { :uri "/" })))
  (is (= 
    { :service "message", :action "list-records" }
    (parse-compiled-route 
      { :route (clout/route-compile "/:service/:action")
        :request-map { :service 'service, :action 'action } }
      { :uri "/message/list_records" }))))

(deftest test-compiled-parse
  (request/set-request-map { :request { :uri "/home/index/1" } }
    (is (= 
      { :service "home", :action "index", :params { :id "1" } }
      (compiled-parse))))
  (request/set-request-map { :request { :uri "/home/index" } }
    (is (= 
      { :service "home", :action "index" } 
      (compiled-parse))))
  (request/set-request-map { :request { :uri "/home" } }
    (is (= 
      { :service "home", :action "index" }
      (compiled-parse))))
  (request/set-request-map { :request { :uri "/" } }
    (is (= 
      { :service "home", :action "index" }
      (compiled-parse))))
  (request/set-request-map { :request { :uri "/home/index/1" :query-string "foo=bar&baz=biz" } }
    (is (= 
      { :service "home", :action "index", :params { :id "1" } }
      (compiled-parse))))
  (request/set-request-map { :request { :uri "/home/index/1/fail" } }
    (is (nil? (compiled-parse)))))

(deftest test-parse-path
  (request/set-request-map { :request { :uri "/home/index/1" } }
    (is (= 
      { :service "home", :action "index", :params { :id "1" } }
      (parse-path))))
  (request/set-request-map { :request { :uri "/home/index/1" } }
    (binding [routes/routes { :functions [test-route-function] }]
      (is (= :route-function (parse-path))))))

(deftest test-call-service
  (is (call-service { :service "home", :action "index" }))
  (request/set-request-map { :service "home", :action "index" }
    (is (call-service {}))))

(deftest test-route-request
  (request/set-request-map { :request { :uri "/home/index" } }
    (is (route-request))))