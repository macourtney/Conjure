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

(deftest test-text-field
  (is (= "<input id=\"message-text\" name=\"message[text]\" type=\"text\" value=\"Blah\" />" (text-field :message :text { :text "Blah" } )))
  (is (= "<input id=\"message-text\" name=\"message[text]\" size=\"20\" type=\"text\" value=\"Blah\" />" (text-field :message :text { :text "Blah" } { :size 20 } ))))
  
(deftest test-text-area
  (is (= "<textarea cols=\"20\" id=\"message-text\" name=\"message[text]\" rows=\"40\">Blah</textarea>" (text-area :message :text { :text "Blah" } )))
  (is (= "<textarea cols=\"40\" id=\"message-text\" name=\"message[text]\" rows=\"60\">Blah</textarea>" (text-area :message :text { :text "Blah" } { :rows 60, :cols 40 } ))))

(deftest test-hidden-field
  (is (= "<input id=\"message-text\" name=\"message[text]\" type=\"hidden\" value=\"Blah\" />" (hidden-field :message :text { :text "Blah" } )))
  (is (= "<input class=\"hidden-message\" id=\"message-text\" name=\"message[text]\" type=\"hidden\" value=\"Blah\" />" (hidden-field :message :text { :text "Blah" } { :class "hidden-message" } ))))

(deftest test-option
  (is (= "<option value=\"test\">test</option>" (option-tag "test")))
  (is (= "<option value=\"blah\">test</option>" (option-tag "test" "blah")))
  (is (= "<option selected=\"true\" value=\"blah\">test</option>"(option-tag "test" "blah" true)))) 