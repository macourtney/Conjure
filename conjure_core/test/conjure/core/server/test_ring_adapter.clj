(ns conjure.core.server.test-ring-adapter
  (:use clojure.contrib.test-is
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
      
(deftest test-conjure
  (is
    (conjure 
      { :uri uri
        :query-string "foo=bar&baz=biz" }))
  (is
    (conjure 
      { :uri "" 
        :query-string "" })))