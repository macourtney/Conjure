(ns test.controller.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.controller.util)
  (:require [generators.controller-generator :as controller-generator]
            [destroyers.controller-destroyer :as controller-destroyer]
            [clojure.contrib.logging :as logging]))

(def controller-name "test")
(def action-name "blah")

(defn setup-all [function]
  (let [generator-map { :controller controller-name, :actions [action-name], :silent true }]
    (controller-generator/generate-controller-file generator-map)
    (load-controller controller-name)
    (function)
    (controller-destroyer/destroy-all-dependencies generator-map)
    (reset! action-interceptors {})))
        
(use-fixtures :once setup-all)
  
(deftest test-find-controllers-directory
  (let [controllers-directory (find-controllers-directory)]
    (is (not (nil? controllers-directory)))
    (is (instance? File controllers-directory))))
    
(deftest test-controller-file-name-string
  (let [controller-file-name (controller-file-name-string controller-name)]
    (is (not (nil? controller-file-name)))
    (is (= "test_controller.clj" controller-file-name)))
  (let [controller-file-name (controller-file-name-string "test-name")]
    (is (not (nil? controller-file-name)))
    (is (= "test_name_controller.clj" controller-file-name)))
  (is (nil? (controller-file-name-string nil)))
  (is (nil? (controller-file-name-string ""))))

(deftest test-controller-file-name
  (is (= (str controller-name "_controller.clj") (controller-file-name { :controller controller-name })))
  (is (nil? (controller-file-name { :controller "" })))
  (is (nil? (controller-file-name { :controller nil })))
  (is (nil? (controller-file-name {}))))
  
(deftest test-controller-from-file
  (let [controller-file (new File "test_controller.clj")
        controller-name (controller-from-file controller-file)]
    (is (not (nil? controller-name)))
    (is (= "test" controller-name)))
  (let [controller-file (new File "test_name_controller.clj")
        controller-name (controller-from-file controller-file)]
    (is (not (nil? controller-name)))
    (is (= "test-name" controller-name)))
  (is (nil? (controller-from-file nil))))
  
(deftest test-find-controller-file
  (let [controllers-directory (find-controllers-directory)]
    (let [controller-file (find-controller-file controllers-directory controller-name)]
      (is (not (nil? controller-file))))
    (let [controller-file (find-controller-file controllers-directory "fail")]
      (is (nil? controller-file)))
    (let [controller-file (find-controller-file controllers-directory nil)]
      (is (nil? controller-file)))
  (let [controller-file (find-controller-file "test")]
    (is (not (nil? controller-file))))))
    
(deftest test-controller-namespace
  (let [controller-ns (controller-namespace controller-name)]
    (is (not (nil? controller-ns)))
    (is (= "controllers.test-controller" controller-ns)))
  (let [controller-ns (controller-namespace "test-name")]
    (is (not (nil? controller-ns)))
    (is (= "controllers.test-name-controller" controller-ns)))
  (let [controller-ns (controller-namespace "test_name")]
    (is (not (nil? controller-ns)))
    (is (= "controllers.test-name-controller" controller-ns)))
  (is (nil? (controller-namespace nil))))

(deftest test-controller-exists?
  (is (controller-exists? (controller-file-name { :controller controller-name })))
  (is (not (controller-exists? (controller-file-name { :controller "fail" })))))

(deftest test-load-controller
  (load-controller controller-name))

(deftest test-fully-qualified-action
  (is (= (str "controllers." controller-name "-controller/" action-name) (fully-qualified-action { :controller controller-name, :action action-name })))
  (is (= nil (fully-qualified-action { :controller controller-name })))
  (is (= nil (fully-qualified-action { })))
  (is (= nil (fully-qualified-action nil))))

(deftest test-method-key
  (is (= :get (method-key { :request { :method "GET" } })))
  (is (= :post (method-key { :request { :method "POST" } })))
  (is (= :put (method-key { :request { :method "PUT" } })))
  (is (= :delete (method-key { :request { :method "DELETE" } }))))

(deftest test-actions-map
  (is (actions-map controller-name))
  (is (nil? (actions-map "fail")))
  (is (nil? (actions-map nil))))

(deftest test-methods-map
  (is (methods-map controller-name action-name))
  (is (nil? (methods-map "fail" action-name)))
  (is (nil? (methods-map nil action-name)))
  (is (nil? (methods-map controller-name "fail"))))

(deftest test-action-function
  (is (action-function controller-name action-name :all))
  (is (action-function controller-name action-name))
  (is (action-function controller-name action-name :default))
  (is (nil? (action-function controller-name "fail" :all)))
  (is (nil? (action-function "fail" action-name :all))))

(deftest test-find-action-fn
  (is (find-action-fn { :controller controller-name, :action action-name, :request { :method "GET" } })))

(deftest test-find-actions
  (is (not-empty (find-actions controller-name)))
  (is (not-empty (find-actions controller-name { :includes #{ (keyword action-name) } })))
  (is (not-empty (find-actions controller-name { :excludes #{ :fail } })))
  (is (empty? (find-actions controller-name { :excludes #{ (keyword action-name) } })))
  (is (empty? (find-actions controller-name { :includes #{ :fail } })))
  (is (empty? (find-actions controller-name { :includes #{ :fail }, :excludes #{ :fail } })))
  (is (not-empty (find-actions controller-name 
    { :includes #{ (keyword action-name) }, :excludes #{ (keyword action-name) } }))))

(defn
#^{ :doc "A simple action for use when testing." }
  test-action [request-map]
  "")
  
(defn
#^{ :doc "A simple action for use when testing." }
  test-action2 [request-map]
  "")

(deftest test-action-fn-method-map
  (is (= { test-action [ :all ] } (action-fn-method-map { :all test-action })))
  (is (= { test-action [ :get :post ] } (action-fn-method-map { :get test-action, :post test-action })))
  (is (= 
    { test-action [ :get ], test-action2 [ :post ] } 
    (action-fn-method-map { :get test-action, :post test-action2 })))
  (is (= {} (action-fn-method-map {})))
  (is (= {} (action-fn-method-map nil))))

(deftest test-assoc-methods
  (let [test-action (fn [request-map] nil)
        params { :action-function test-action }]
    (is (= { :all test-action } (assoc-methods {} (assoc params :methods [:all]))))
    (is (= { :get test-action } (assoc-methods {} (assoc params :methods [:get]))))
    (is (= 
      { :get test-action, :put test-action } 
      (assoc-methods {} (assoc params :methods [:get :put]))))
    (is (= 
      { :get test-action, :put test-action } 
      (assoc-methods { :get test-action } (assoc params :methods [:put]))))
    (is (= {} (assoc-methods {} (assoc params :methods []))))
    (is (= { :all test-action } (assoc-methods {} params)))))

(deftest test-assoc-actions
  (let [test-action (fn [request-map] nil)
        params { :action-function test-action, :methods [:all] }
        method-map { :all test-action }]
    (is (= { (keyword action-name) method-map } (assoc-actions {} (assoc params :action action-name))))
    (is (= { :blah method-map } (assoc-actions {} (assoc params :action :blah))))
    (is (= 
      { (keyword action-name) method-map, :foo method-map } 
      (assoc-actions { :foo method-map } (assoc params :action action-name))))))

(deftest test-assoc-controllers
  (let [test-action (fn [request-map] nil)
        params { :action action-name, :action-function test-action, :methods [:all] }
        action-map { (keyword action-name) { :all test-action } }]
    (is (= 
      { (keyword controller-name) action-map } 
      (assoc-controllers {} (assoc params :controller controller-name))))
    (is (= 
      { (keyword controller-name) action-map, :foo action-map } 
      (assoc-controllers { :foo action-map } (assoc params :controller controller-name))))))

(deftest test-add-action-function
  (let [initial-controller-actions @controller-actions
        test-action (fn [request-map] nil)
        params { :controller controller-name, :action action-name }
        controller-map { (keyword controller-name) { (keyword action-name) { :all test-action } } }] 
    (reset! controller-actions {})
    (add-action-function test-action (assoc params :methods [:all]))
    (is (= controller-map @controller-actions))
    (reset! controller-actions {})
    (add-action-function test-action params)
    (is (= controller-map @controller-actions))
    (reset! controller-actions initial-controller-actions)))

(deftest test-copy-actions
  (let [initial-controller-actions @controller-actions
        params { :controller controller-name, :action action-name }]
    (reset! controller-actions {})
    (add-action-function test-action (assoc params :methods [:all]))
    (copy-actions "test2" "test")
    (is (= { (keyword action-name) { :all test-action } } (:test2 @controller-actions)))
    (reset! controller-actions {})
    
    (add-action-function test-action (assoc params :methods [:all]))
    (copy-actions "test2" "test" { :includes #{ (keyword action-name) } })
    (is (= { (keyword action-name) { :all test-action } } (:test2 @controller-actions)))
    (reset! controller-actions initial-controller-actions)))

(defn create-stack-interceptor [value]
  (fn [request-map action-fn]
    (cons value (action-fn request-map))))

(defn list-action [request-map]
  (list))

(deftest test-chain-interceptors
  (is (= '("one") ((chain-interceptors (create-stack-interceptor "one")) {} list-action)))
  (is (= '("one" "two")
    ((chain-interceptors
      (create-stack-interceptor "one")
      (create-stack-interceptor "two"))
    {} list-action)))
  (is (= '("two") 
    ((chain-interceptors 
      nil 
      (create-stack-interceptor "two"))
    {} list-action)))
  (is (= '("one")
    ((chain-interceptors
      (create-stack-interceptor "one")
      nil)
    {} list-action)))
  
  (is (= '("one" "two" "three")
    ((chain-interceptors
      (create-stack-interceptor "one")
      (create-stack-interceptor "two")
      (create-stack-interceptor "three"))
    {} list-action)))
  (is (= '("one" "two" "three" "four")
    ((chain-interceptors
      (create-stack-interceptor "one")
      (create-stack-interceptor "two")
      (create-stack-interceptor "three")
      (create-stack-interceptor "four"))
    {} list-action)))
  (is (= '("one" "two" "three" "four")
    ((chain-interceptors
      (create-stack-interceptor "one")
      nil
      (create-stack-interceptor "two")
      nil
      (create-stack-interceptor "three")
      nil
      nil
      (create-stack-interceptor "four"))
    {} list-action)))
  (is (nil? (chain-interceptors nil)))
  (is (nil? (chain-interceptors nil nil))))

(defn pass-interceptor [request-map action-fn]
  (action-fn request-map))

(deftest test-assoc-action-interceptors
  (is (= { :show pass-interceptor } (assoc-action-interceptors {} pass-interceptor :show)))
  (is (= '("one" "two")
    ((:show 
      (assoc-action-interceptors { :show (create-stack-interceptor "two") } 
        (create-stack-interceptor "one") :show))
      {} list-action))))

(deftest test-assoc-controller-interceptors
  (is (= { :test { :show pass-interceptor } } (assoc-controller-interceptors {} pass-interceptor :test :show)))
  (is (= { :test { :show pass-interceptor, :hide pass-interceptor } } 
    (assoc-controller-interceptors { :test { :hide pass-interceptor } } pass-interceptor :test :show)))
  (is (= '("one" "two")
    ((:show (:test 
      (assoc-controller-interceptors { :test { :show (create-stack-interceptor "two") } } 
        (create-stack-interceptor "one") :test :show)))
      {} list-action))))

(deftest test-add-action-interceptor
  (let [initial-action-interceptors @action-interceptors]
    (reset! action-interceptors {})
    (add-action-interceptor pass-interceptor :test :show)
    (is (= { :test { :show pass-interceptor } } @action-interceptors))
    (reset! action-interceptors initial-action-interceptors)))

(deftest test-update-exclude-interceptor-list
  (is (= [{ :interceptor pass-interceptor }] (update-exclude-interceptor-list [] pass-interceptor nil)))
  (is (= [{ :interceptor pass-interceptor, :excludes #{} }] (update-exclude-interceptor-list [] pass-interceptor #{})))
  (is (= [{ :interceptor pass-interceptor, :excludes #{ :show } }] 
    (update-exclude-interceptor-list [] pass-interceptor #{ :show })))
  (is (= [{ :interceptor pass-interceptor, :excludes #{ :show } } { :interceptor pass-interceptor }] 
    (update-exclude-interceptor-list [{ :interceptor pass-interceptor }] pass-interceptor #{ :show }))))

(deftest test-assoc-controller-excludes-interceptors
  (is (= { :test [{ :interceptor pass-interceptor }]}
    (assoc-controller-excludes-interceptors {} pass-interceptor :test nil)))
  (is (= { :test [{ :interceptor pass-interceptor } { :interceptor pass-interceptor }]}
    (assoc-controller-excludes-interceptors { :test [{ :interceptor pass-interceptor }] } pass-interceptor :test nil))))

(deftest test-add-controller-interceptor
  (let [initial-controller-interceptors @controller-interceptors]
    (reset! controller-interceptors {})
    (add-controller-interceptor pass-interceptor :test #{ :show })
    (is (= { :test [{ :excludes #{ :show }, :interceptor pass-interceptor }] } @controller-interceptors))
    (reset! controller-interceptors initial-controller-interceptors)))

(deftest test-add-interceptor
  (let [initial-action-interceptors @action-interceptors
        initial-controller-interceptors @controller-interceptors]
    (reset! action-interceptors {})
    (reset! controller-interceptors {})
    (add-interceptor pass-interceptor :test nil [ :show ])
    (is (= { :test { :show pass-interceptor } } @action-interceptors))
    (is (= {} @controller-interceptors))
    (reset! action-interceptors {})
    (add-interceptor pass-interceptor :test #{ :show } nil)
    (is (= {} @action-interceptors))
    (is (= { :test [{ :excludes #{ :show }, :interceptor pass-interceptor }] } @controller-interceptors))
    (reset! action-interceptors initial-action-interceptors)
    (reset! controller-interceptors initial-controller-interceptors)))

(deftest test-app-interceptor-map
  (is (= 
    { :interceptor pass-interceptor, :excludes { :test #{ :show } } } 
    (app-interceptor-map pass-interceptor { :test #{ :show } })))
  (is (= 
    { :interceptor pass-interceptor, :excludes { :test #{} } } 
    (app-interceptor-map pass-interceptor { :test #{} }))))

(deftest test-add-app-interceptor-to-list
  (is (= 
    [{ :interceptor pass-interceptor, :excludes { :test #{ :show } } }]
    (add-app-interceptor-to-list [] pass-interceptor { :test #{ :show } })))
  (is (= 
    [ { :interceptor pass-interceptor, :excludes { :test2 #{} } }
      { :interceptor pass-interceptor, :excludes { :test #{ :show } } } ]
    (add-app-interceptor-to-list 
      [ { :interceptor pass-interceptor, :excludes { :test #{ :show } } } ] 
      pass-interceptor 
      { :test2 #{} }))))

(deftest test-add-app-interceptor
  (let [initial-app-interceptors @app-interceptors]
    (reset! app-interceptors [])
    (add-app-interceptor pass-interceptor { :test #{ :show } })
    (is (= 
      [ { :interceptor pass-interceptor, :excludes { :test #{ :show } } } ]
      @app-interceptors))
    (reset! app-interceptors initial-app-interceptors)))

(deftest test-find-action-interceptor
  (let [initial-action-interceptors @action-interceptors]
    (reset! action-interceptors {})
    (add-action-interceptor pass-interceptor :test :show)
    (is (= pass-interceptor (find-action-interceptor :test :show)))
    (is (nil? (find-action-interceptor :test :hide)))
    (reset! action-interceptors initial-action-interceptors)))

(deftest test-find-controller-interceptors
  (let [initial-controller-interceptors @controller-interceptors]
    (reset! controller-interceptors {})
    (add-controller-interceptor pass-interceptor :test #{ :show })
    (is (= [pass-interceptor] (find-controller-interceptors :test :hide)))
    (is (= [] (find-controller-interceptors :test :show)))
    (reset! controller-interceptors initial-controller-interceptors)))

(deftest test-call-app-interceptor?
  (is (call-app-interceptor? { :excludes { :test #{ :hide } } } :test :show))
  (is (not (call-app-interceptor? { :excludes { :test #{ :show } } } :test :show)))
  (is (not (call-app-interceptor? { :excludes { :test #{} } } :test :show)))
  (is (not (call-app-interceptor? { :excludes { :test :blah } } :test :show)))
  (is (call-app-interceptor? { :excludes { } } :test :show)))

(deftest test-valid-app-interceptors
  (is (= [pass-interceptor] (valid-app-interceptors [{ :interceptor pass-interceptor, :excludes {} }] :test :show)))
  (is (= [] (valid-app-interceptors [{ :interceptor pass-interceptor, :excludes { :test #{ :show } } }] :test :show)))
  (is (= 
    [pass-interceptor] 
    (valid-app-interceptors 
      [ { :interceptor pass-interceptor, :excludes { :test #{ :show } } } 
        { :interceptor pass-interceptor, :excludes {} } ]
      :test
      :show)))
  (is (= 
    [pass-interceptor] 
    (valid-app-interceptors 
      [ { :interceptor pass-interceptor, :excludes { :test #{ :hide } } } 
        { :interceptor pass-interceptor, :excludes { :test #{} } } ]
      :test
      :show)))
  (is (= 
    [pass-interceptor pass-interceptor] 
    (valid-app-interceptors 
      [ { :interceptor pass-interceptor, :excludes { :test #{ :hide } } } 
        { :interceptor pass-interceptor, :excludes {} } ]
      :test
      :show))))

(deftest test-find-app-interceptors
  (let [initial-app-interceptors @app-interceptors]
    (reset! app-interceptors [])
    (add-app-interceptor pass-interceptor { :test #{ :hide } })
    (is (= [pass-interceptor] (find-app-interceptors :test :show)))
    (is (= [] (find-app-interceptors :test :hide)))
    (reset! app-interceptors initial-app-interceptors)))

(deftest test-run-action
  (is (run-action { :controller controller-name, :action action-name, :request { :method "GET" } })))

(deftest test-call-controller
  (is (call-controller { :controller controller-name, :action action-name, :request { :method "GET" } }))
  (let [initial-controller-actions @controller-actions]
    (reset! controller-actions {})
    (is (call-controller { :controller controller-name, :action action-name, :request { :method "GET" } }))
    (reset! controller-actions initial-controller-actions)))