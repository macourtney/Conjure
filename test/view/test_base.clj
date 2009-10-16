(ns test.view.test-base
  (:use clojure.contrib.test-is
        conjure.view.base))

(defview [message]
  message)

(deftest test-defview
  (is (= "test" (render-view {} "test"))))

(deftest test-url-for
  (is (= "/hello/show" (url-for { :controller "hello", :action "show" })))
  (is (= "/hello/show/1" (url-for { :controller "hello", :action "show", :id 1})))
  (is (= "/hello/show/1" (url-for { :controller "hello", :action "show", :id { :id 1 }})))
  (is (= "/hello/show/#message" (url-for { :controller "hello", :action "show", :anchor "message"})))
  (is (= "/hello/show/1/#message" (url-for { :controller "hello", :action "show", :id 1, :anchor "message"})))
  (is (= "/hello/show/1" (url-for { :controller "hello", :action "show" } { :id 1 })))
  (is (= "/hello/show/1" (url-for { :controller "hello", :action "add" } { :action "show", :id 1 })))
  (let [params { :controller "hello", :action "show" :id 1}]
    (is (= "http://localhost/hello/show/1" (url-for { :server-name "localhost" } params)))
    (is (= "http://localhost:8080/hello/show/1" (url-for { :server-name "localhost" :server-port 8080 } params)))
    (is (= "ftp://localhost/hello/show/1" (url-for { :server-name "localhost" :scheme :ftp } params)))
    (is (= "http://localhost:8080/hello/show/1" (url-for { :server-name "localhost" } (merge params { :port 8080}))))
    (is (= "http://foo:bar@localhost/hello/show/1" (url-for { :server-name "localhost" } (merge params { :user "foo", :password "bar"}))))))

(deftest test-link-to
  (is (= "<a href=\"/hello/show\">view</a>" (link-to "view" { :controller "hello" :action "show" })))
  (is (= "<a href=\"/hello/show\">view</a>" (link-to "view" { :controller "hello" :action "add" } { :action "show" })))
  (is (= "<a class=\"bar\" href=\"/hello/show\" id=\"foo\">view</a>" (link-to "view" { :controller "hello" :action "show" :html-options { :id "foo" :class "bar" } })))
  (is (= "<a href=\"/hello/show\">show</a>" (link-to #(:action %) { :controller "hello" :action "show" }))))

(deftest test-link-to-if
  (is (= "<a href=\"/hello/show\">view</a>" (link-to-if true "view" { :controller "hello" :action "show" })))
  (is (= "view" (link-to-if false "view" { :controller "hello" :action "show" })))
  (is (= "show" (link-to-if false #(:action %) { :controller "hello" :action "show" })))
  (is (= "<a href=\"/hello/show\">view</a>" (link-to-if #(= (:action %) "show") "view" { :controller "hello" :action "show" })))
  (is (= "view" (link-to-if #(= (:action %) "add") "view" { :controller "hello" :action "show" }))))
  
(deftest test-link-to-unless
  (is (= "view" (link-to-unless true "view" { :controller "hello" :action "show" })))
  (is (= "<a href=\"/hello/show\">view</a>" (link-to-unless false "view" { :controller "hello" :action "show" })))
  (is (= "show" (link-to-unless true #(:action %) { :controller "hello" :action "show" })))
  (is (= "view" (link-to-unless #(= (:action %) "show") "view" { :controller "hello" :action "show" })))
  (is (= "<a href=\"/hello/show\">view</a>" (link-to-unless #(= (:action %) "add") "view" { :controller "hello" :action "show" }))))

(deftest test-form-for
  (is (= "<form action=\"/hello/create\" method=\"put\" name=\"create\">Blah</form>" (form-for { :name "create", :url { :controller "hello", :action "create" } } "Blah")))
  (is (= "<form action=\"/hello/create\" method=\"put\" name=\"hello\">Blah</form>" (form-for { :url { :controller "hello", :action "create" } } "Blah")))
  (is (= "<form action=\"/hello/create\" method=\"put\" name=\"create\">create</form>" (form-for { :name "create", :url { :controller "hello", :action "create" } } #(:action %)))))