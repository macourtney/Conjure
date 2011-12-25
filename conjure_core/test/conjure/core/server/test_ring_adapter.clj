(ns conjure.core.server.test-ring-adapter
  (:use clojure.test
        conjure.core.server.ring-adapter
        test-helper))
        
(def controller-name "test")
(def action-name "show")
(def uri (str controller-name "/" action-name "/1"))

(use-fixtures :once init-server)

(deftest test-call-server
  (is
    (call-server 
      { :uri uri
        :query-string "foo=bar&baz=biz" })))

(defn noop-app [request]
  nil)

(deftest test-wrap-resource-dir
  (is ((wrap-resource-dir noop-app "public") { :request-method :get, :uri "/stylesheets/main.css" }))
  (is (nil? ((wrap-resource-dir noop-app "public") { :request-method :get, :uri "/" }))))

(deftest test-conjure
  (is
    (conjure 
      { :uri uri
        :query-string "foo=bar&baz=biz" }))
  (is
    (conjure 
      { :uri "" 
        :query-string "" })))