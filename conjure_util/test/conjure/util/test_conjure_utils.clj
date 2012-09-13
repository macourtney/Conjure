(ns conjure.util.test-conjure-utils
  (:import [java.io File])
  (:use clojure.test
        conjure.util.conjure-utils)
  (:require [config.session-config :as session-config]
            [conjure.util.request :as request]
            [controllers.home-controller :as home-controller]))

(def home-controller-file (.getAbsoluteFile (File. "test/controllers/home_controller.clj")))
(def home-controller-namespace-map (create-file-namespace-map home-controller-file))
(def home-controller-namespace-info (namespace-info home-controller-namespace-map))

(deftest test-conjure-namespaces
  (is (= #{"helpers.home-helper"} (conjure-namespaces 'controllers.home-controller))))

(deftest test-file-namespaces
  (is (file-namespaces)))

(deftest test-filter-file-namespaces
  (is (= [home-controller-namespace-map]
         (filter-file-namespaces ["controllers.home-controller"])))
  (is (= [] (filter-file-namespaces ["clojure.xml"])))
  (is (= [home-controller-namespace-map]
         (filter-file-namespaces ["clojure.xml" "controllers.home-controller"])))
  (is (= [] (filter-file-namespaces [])))
  (is (= [] (filter-file-namespaces nil))))

(deftest test-loaded-namespaces
  (clear-loaded-namespaces)
  (is (not (namespace-loaded? "controllers.home-controller")))
  (is (nil? (namespace-load-info "controllers.home-controller")))
  (add-namespace-info (create-file-namespace-map home-controller-file))
  (is (namespace-loaded? "controllers.home-controller"))
  (is (= home-controller-namespace-info (namespace-load-info "controllers.home-controller")))
  (is (not (reload-namespace? "controllers.home-controller")))
  (is (not (reload-namespace-map? home-controller-namespace-map)))
  (is (= [] (namespaces-to-reload ["controllers.home-controller" "clojure.xml"])))
  (clear-loaded-namespaces)
  (is (not (reload-namespace? "controllers.home-controller")))
  (is (not (reload-namespace-map? home-controller-namespace-map)))
  (is (= [] (namespaces-to-reload ["controllers.home-controller" "clojure.xml"])))
  (add-namespace-info
    (assoc home-controller-namespace-map :last-modified (- (.lastModified home-controller-file) 10000)))
  (is (reload-namespace? "controllers.home-controller"))
  (is (reload-namespace-map? home-controller-namespace-map))
  (is (= [home-controller-namespace-map] (namespaces-to-reload ["controllers.home-controller" "clojure.xml"])))
  (clear-loaded-namespaces))

(deftest test-reload-conjure-namespaces
  (clear-loaded-namespaces)
  (reload-conjure-namespaces "controllers.home-controller")
  (is (namespace-loaded? "controllers.home-controller"))
  (is (namespace-loaded? "helpers.home-helper"))
  (clear-loaded-namespaces))

(deftest test-merge-url-for-params
  (is (= 
    { :service "hello", :action "edit", :params { :id 0 } } 
    (merge-url-for-params 
      { :service "hello", :action "show", :params { :id 1 } } 
      { :action "edit", :params { :id 0 } })))
  (is (= 
    { :service "hello", :action "edit" } 
    (merge-url-for-params 
      { :service "hello", :action "show", :params { :id 1 } } 
      { :action "edit" })))
  (is (= 
    { :service "hello", :action "edit", :params { :id 0 } } 
    (merge-url-for-params 
      { :service "hello", :action "show", :params { :id 1, :text "blah" } } 
      { :action "edit", :params { :id 0 } }))))

(deftest test-url-for
  (request/set-request-map { :service "hello", :action "show" }
    (is (= "/hello/show" (url-for))))
  (request/set-request-map { :service "hello", :action "show", :params { :id 1 } }
    (is (= "/hello/show" (url-for))))
  (request/set-request-map { :service "hello", :action "show", :params { :id { :id 1 } } }
    (is (= "/hello/show" (url-for))))
  (request/set-request-map { :service "hello", :action "show", :anchor "message"}
    (is (= "/hello/show" (url-for))))
  (request/set-request-map { :service "hello", :action "show", :params { :id 1 }, :anchor "message"}
    (is (= "/hello/show" (url-for))))
  (request/set-request-map { :service "hello", :action "show", :anchor "message"}
    (is (= "/hello/show/#message" (url-for { :anchor "message" }))))
  (request/set-request-map { :service "hello", :action "show" }
    (is (= "/hello/show/1/#message" (url-for { :params { :id 1 }, :anchor "message" }))))
  (request/set-request-map { :service "hello", :action "show" }
    (is (= "/hello/show/1" (url-for { :params { :id 1 } }))))
  (request/set-request-map { :service "hello", :action "show" }
    (is (= "/hello/show/1" (url-for { :params { :id { :id 1 } } }))))
  (request/set-request-map { :service "hello", :action "add" }
    (is (= "/hello/show/1" (url-for { :action "show", :params { :id 1 } }))))
  (let [params { :service "hello", :action "show", :params { :id 1 } }]
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
  (let [params { :service "hello", :action "show", :params { :id 1, :session-id "blah" } }]
    (binding [session-config/use-session-cookie false]
      (request/set-request-map { :request { :server-name "localhost" } }
        (is (= "http://localhost/hello/show/1?session-id=blah" (url-for params)))))))