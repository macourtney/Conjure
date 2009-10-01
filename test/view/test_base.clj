(ns test.view.test-base
  (:use clojure.contrib.test-is
        conjure.view.base))

(defview [message]
  message)

(deftest test-defview
  (is (= "test" (render-view {} "test"))))

(deftest test-url-for
  (is (= "/hello/show" (url-for { :controller "hello" :action "show" })))
  (is (= "/hello/show/1" (url-for { :controller "hello" :action "show" :id 1})))
  (is (= "/hello/show/1" (url-for { :controller "hello" :action "show" :id { :id 1 }})))
  (is (= "/hello/show/#message" (url-for { :controller "hello" :action "show" :anchor "message"})))
  (is (= "/hello/show/1/#message" (url-for { :controller "hello" :action "show" :id 1 :anchor "message"})))
  (is (= "/hello/show/1" (url-for { :controller "hello" :action "show" } { :id 1 })))
  (is (= "/hello/show/1" (url-for { :controller "hello" :action "add" } { :action "show" :id 1 }))))

(deftest test-link-to
  (is (= "<a href=\"/hello/show\">view</a>" (link-to "view" { :controller "hello" :action "show" })))
  (is (= "<a href=\"/hello/show\">view</a>" (link-to "view" { :controller "hello" :action "add" } { :action "show" }))))