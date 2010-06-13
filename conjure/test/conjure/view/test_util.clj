(ns conjure.view.test-util
  (:import [java.io File])
  (:use test-helper
        clojure.contrib.test-is
        conjure.view.util)
  (:require [clojure.contrib.logging :as logging]
            [config.session-config :as session-config]
            [conjure.server.request :as request]))

(def action-name "show")
(def controller-name "test")

(defn setup-all [function]
  (function))
        
(use-fixtures :once setup-all)

(deftest test-find-views-directory
  (test-directory (find-views-directory) "views"))

(deftest test-view-files
  (doseq [view-file (view-files)]
    (is (.isFile view-file))
    (is (.endsWith (.getName view-file) ".clj"))))

(deftest test-find-controller-directory
  (test-directory (find-controller-directory (find-views-directory) controller-name) controller-name)
  (test-directory (find-controller-directory controller-name) controller-name)
  (is (nil? (find-controller-directory nil))))
  
(deftest test-find-view-file
  (let [controller-directory (find-controller-directory controller-name)]
    (test-file 
      (find-view-file controller-directory action-name) 
      (str action-name ".clj"))
    (is (nil? (find-view-file controller-directory nil)))
    (is (nil? (find-view-file nil action-name)))
    (is (nil? (find-view-file nil nil)))))
    
(deftest test-load-view
  (request/with-controller-action controller-name action-name
    (load-view))
  (load-view controller-name action-name))

(deftest test-request-view-namespace
  (request/with-controller-action controller-name action-name
    (is (= (str "views." controller-name "." action-name) 
         (request-view-namespace))))
  (is (= (str "views." controller-name "." action-name) 
         (request-view-namespace controller-name action-name)))
  (is (= "views.test-foo.show-foo" 
         (request-view-namespace "test-foo" "show-foo")))
  (is (= "views.test-foo.show-foo" 
         (request-view-namespace "test_foo" "show_foo")))
  (is (nil? (request-view-namespace nil nil))))
  
(deftest test-view-namespace
  (is (nil? (view-namespace nil)))
  (is (= (str "views." controller-name "." action-name) 
         (view-namespace (new File (find-views-directory) (str controller-name "/" action-name ".clj")))))
  (is (= (str "views.my-controller.action-view") 
         (view-namespace (new File (find-views-directory) "my_controller/action_view.clj")))))

(deftest test-all-view-namespaces
  (when-let [view-namespaces (all-view-namespaces)]
    (doseq [view-namespace view-namespaces]
      (is view-namespace))))
  
(deftest test-merge-url-for-params
  (is (= 
    { :controller "hello", :action "edit", :params { :id 0 } } 
    (merge-url-for-params 
      { :controller "hello", :action "show", :params { :id 1 } } 
      { :action "edit", :params { :id 0 } })))
  (is (= 
    { :controller "hello", :action "edit" } 
    (merge-url-for-params 
      { :controller "hello", :action "show", :params { :id 1 } } 
      { :action "edit" })))
  (is (= 
    { :controller "hello", :action "edit", :params { :id 0 } } 
    (merge-url-for-params 
      { :controller "hello", :action "show", :params { :id 1, :text "blah" } } 
      { :action "edit", :params { :id 0 } }))))

(deftest test-url-for
  (request/set-request-map { :controller "hello", :action "show" }
    (is (= "/hello/show" (url-for))))
  (request/set-request-map { :controller "hello", :action "show", :params { :id 1 } }
    (is (= "/hello/show" (url-for))))
  (request/set-request-map { :controller "hello", :action "show", :params { :id { :id 1 } } }
    (is (= "/hello/show" (url-for))))
  (request/set-request-map { :controller "hello", :action "show", :anchor "message"}
    (is (= "/hello/show" (url-for))))
  (request/set-request-map { :controller "hello", :action "show", :params { :id 1 }, :anchor "message"}
    (is (= "/hello/show" (url-for))))
  (request/set-request-map { :controller "hello", :action "show", :anchor "message"}
    (is (= "/hello/show/#message" (url-for { :anchor "message" }))))
  (request/set-request-map { :controller "hello", :action "show" }
    (is (= "/hello/show/1/#message" (url-for { :params { :id 1 }, :anchor "message" }))))
  (request/set-request-map { :controller "hello", :action "show" }
    (is (= "/hello/show/1" (url-for { :params { :id 1 } }))))
  (request/set-request-map { :controller "hello", :action "show" }
    (is (= "/hello/show/1" (url-for { :params { :id { :id 1 } } }))))
  (request/set-request-map { :controller "hello", :action "add" }
    (is (= "/hello/show/1" (url-for { :action "show", :params { :id 1 } }))))
  (let [params { :controller "hello", :action "show", :params { :id 1 } }]
    (request/set-request-map { :request { :server-name "localhost" } }
      (is (= "http://localhost/hello/show/1" (url-for params))))
    (request/set-request-map { :request { :server-name "localhost" :server-port 8080 } }
      (is (= "http://localhost:8080/hello/show/1" (url-for params))))
    (request/set-request-map { :request { :server-name "localhost" :scheme :ftp } }
      (is (= "ftp://localhost/hello/show/1" (url-for params))))
    (request/set-request-map { :request { :server-name "localhost" } }
      (is (= "http://localhost:8080/hello/show/1" (url-for (merge params { :port 8080})))))
    (request/set-request-map { :request { :server-name "localhost" } }
      (is (= "http://foo:bar@localhost/hello/show/1" (url-for (merge params { :user "foo", :password "bar"}))))))
  (let [params { :controller "hello", :action "show", :params { :id 1, :session-id "blah" } }]
    (binding [session-config/use-session-cookie false]
      (request/set-request-map { :request { :server-name "localhost" } }
        (is (= "http://localhost/hello/show/1?session-id=blah" (url-for params)))))))