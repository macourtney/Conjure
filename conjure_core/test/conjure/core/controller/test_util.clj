(ns conjure.core.controller.test-util
  (:import [java.io File])
  (:use clojure.contrib.test-is
        conjure.core.controller.util
        test-helper)
  (:require [clojure.contrib.logging :as logging]
            [clojure.contrib.seq-utils :as seq-utils]
            [conjure.core.server.request :as request]))

(def controller-name "test")
(def action-name "show")

(use-fixtures :once init-server)
  
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
  (request/set-request-map { :controller controller-name }
    (is (= (str controller-name "_controller.clj") (controller-file-name))))
  (request/set-request-map { :controller "" }
    (is (nil? (controller-file-name))))
  (request/set-request-map { :controller nil }
    (is (nil? (controller-file-name))))
  (is (nil? (controller-file-name))))
  
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

(deftest test-is-controller-namespace?
  (is (is-controller-namespace? "controllers.test-name-controller"))
  (is (not (is-controller-namespace? "fail")))
  (is (not (is-controller-namespace? "")))
  (is (not (is-controller-namespace? nil))))

(defn find-first-str [string str-seq]
  (seq-utils/find-first #(= string %) str-seq))

(deftest test-all-controller-namespaces
  (let [controller-namespaces (all-controller-namespaces)]
    (is (= 3 (count controller-namespaces)))
    (is (find-first-str "controllers.test-controller"
          (map #(name (ns-name %)) controller-namespaces)))))

(deftest test-controller-from-namespace
  (is (= "test" (controller-from-namespace "controllers.test-controller")))
  (is (nil? (controller-from-namespace nil))))

(deftest test-all-controllers
  (let [controllers (all-controllers)]
    (println "controllers:" controllers)
    (is (= 3 (count controllers)))
    (is (find-first-str "test" controllers))
    (is (find-first-str "template" controllers))
    (is (find-first-str "home" controllers))))

(deftest test-controller-exists?
  (request/set-request-map { :controller controller-name }
    (is (controller-exists? (controller-file-name))))
  (request/set-request-map { :controller "fail" }
    (is (not (controller-exists? (controller-file-name))))))

(deftest test-load-controller
  (load-controller controller-name))

(deftest test-fully-qualified-action
  (request/set-request-map { :controller controller-name, :action action-name }
    (is (= (str "controllers." controller-name "-controller/" action-name) (fully-qualified-action))))
  (request/set-request-map { :controller controller-name }
    (is (nil? (fully-qualified-action))))
  (request/set-request-map { }
    (is (nil? (fully-qualified-action))))
  (request/set-request-map nil
    (is (nil? (fully-qualified-action)))))

(deftest test-method-key
  (request/set-request-map { :request { :method "GET" } }
    (is (= :get (method-key))))
  (request/set-request-map { :request { :method "POST" } }
    (is (= :post (method-key))))
  (request/set-request-map { :request { :method "PUT" } }
    (is (= :put (method-key))))
  (request/set-request-map { :request { :method "DELETE" } }
    (is (= :delete (method-key)))))

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
  (request/set-request-map { :controller controller-name, :action action-name, :request { :method "GET" } }
    (is (find-action-fn))))

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
  (fn [action-fn]
    (cons value (action-fn))))

(defn list-action []
  (list))

(deftest test-chain-interceptors
  (is (= '("one") ((chain-interceptors (create-stack-interceptor "one")) list-action)))
  (is (= '("one" "two")
    ((chain-interceptors
      (create-stack-interceptor "one")
      (create-stack-interceptor "two"))
      list-action)))
  (is (= '("two") 
    ((chain-interceptors 
      nil 
      (create-stack-interceptor "two"))
      list-action)))
  (is (= '("one")
    ((chain-interceptors
      (create-stack-interceptor "one")
      nil)
      list-action)))
  
  (is (= '("one" "two" "three")
    ((chain-interceptors
      (create-stack-interceptor "one")
      (create-stack-interceptor "two")
      (create-stack-interceptor "three"))
      list-action)))
  (is (= '("one" "two" "three" "four")
    ((chain-interceptors
      (create-stack-interceptor "one")
      (create-stack-interceptor "two")
      (create-stack-interceptor "three")
      (create-stack-interceptor "four"))
      list-action)))
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
      list-action)))
  (is (nil? (chain-interceptors nil)))
  (is (nil? (chain-interceptors nil nil))))

(defn pass-interceptor [request-map action-fn]
  (action-fn request-map))

(deftest test-assoc-action-interceptors
  (is (= { :show { :pass-interceptor pass-interceptor } } 
        (assoc-action-interceptors {} pass-interceptor :pass-interceptor :show)))
  (let [one-interceptor (create-stack-interceptor "one")
        two-interceptor (create-stack-interceptor "two")] 
    (is (= { :show { :one one-interceptor, :two two-interceptor }}
      (assoc-action-interceptors { :show { :two two-interceptor } } one-interceptor :one :show)))))

(deftest test-assoc-controller-interceptors
  (is (= { :test { :show { :pass-interceptor pass-interceptor } } } 
        (assoc-controller-interceptors {} pass-interceptor :pass-interceptor :test :show)))
  (is (= { :test { :show { :pass-interceptor pass-interceptor }, :hide { :pass-interceptor pass-interceptor } } } 
        (assoc-controller-interceptors { :test { :hide { :pass-interceptor pass-interceptor } } } 
          pass-interceptor :pass-interceptor :test :show)))
  (let [interceptor-one (create-stack-interceptor "one")
        interceptor-two (create-stack-interceptor "two")]
    (is (= { :test { :show { :one interceptor-one, :two interceptor-two } } }
          (assoc-controller-interceptors { :test { :show { :two interceptor-two } } } 
            interceptor-one :one :test :show)))))

(deftest test-add-action-interceptor
  (let [initial-action-interceptors @action-interceptors]
    (reset! action-interceptors {})
    (add-action-interceptor pass-interceptor :pass-interceptor :test :show)
    (is (= { :test { :show { :pass-interceptor pass-interceptor} } } @action-interceptors))
    (reset! action-interceptors initial-action-interceptors)))

(deftest test-update-exclude-interceptor-list
  (is (= { :pass-interceptor { :interceptor pass-interceptor } }
        (update-exclude-interceptor-list {} pass-interceptor :pass-interceptor nil)))
  (is (= { :pass-interceptor { :interceptor pass-interceptor, :excludes #{} } } 
        (update-exclude-interceptor-list {} pass-interceptor :pass-interceptor #{})))
  (is (= { :pass-interceptor { :interceptor pass-interceptor, :excludes #{ :show } } } 
    (update-exclude-interceptor-list {} pass-interceptor :pass-interceptor #{ :show })))
  (is (= { :pass-interceptor { :interceptor pass-interceptor, :excludes #{ :show } } 
           :pass-interceptor2 { :interceptor pass-interceptor } } 
        (update-exclude-interceptor-list { :pass-interceptor2 { :interceptor pass-interceptor } } 
          pass-interceptor :pass-interceptor #{ :show }))))

(deftest test-assoc-controller-excludes-interceptors
  (is (= { :test { :pass-interceptor { :interceptor pass-interceptor } } }
    (assoc-controller-excludes-interceptors {} pass-interceptor :pass-interceptor :test nil)))
  (is (= { :test { :pass-interceptor { :interceptor pass-interceptor } } }
    (assoc-controller-excludes-interceptors { :test { :pass-interceptor { :interceptor pass-interceptor } } }
      pass-interceptor :pass-interceptor :test nil)))
  (is (= { :test { :pass-interceptor { :interceptor pass-interceptor } 
                   :pass-interceptor2 { :interceptor pass-interceptor } } }
    (assoc-controller-excludes-interceptors 
      { :test 
        { :pass-interceptor { :interceptor pass-interceptor } 
          :pass-interceptor2 { :interceptor pass-interceptor } } }
      pass-interceptor :pass-interceptor2 :test nil))))

(deftest test-add-controller-interceptor
  (let [initial-controller-interceptors @controller-interceptors]
    (reset! controller-interceptors {})
    (add-controller-interceptor pass-interceptor :pass-interceptor :test #{ :show })
    (is (= { :test { :pass-interceptor { :excludes #{ :show }, :interceptor pass-interceptor } } }
          @controller-interceptors))
    (reset! controller-interceptors initial-controller-interceptors)))

(deftest test-add-interceptor
  (let [initial-action-interceptors @action-interceptors
        initial-controller-interceptors @controller-interceptors]
    (reset! action-interceptors {})
    (reset! controller-interceptors {})
    (add-interceptor pass-interceptor :pass-interceptor :test nil [ :show ])
    (is (= { :test { :show { :pass-interceptor pass-interceptor } } } @action-interceptors))
    (is (= {} @controller-interceptors))
    (reset! action-interceptors {})
    (add-interceptor pass-interceptor :pass-interceptor :test #{ :show } nil)
    (is (= {} @action-interceptors))
    (is (= { :test { :pass-interceptor { :excludes #{ :show }, :interceptor pass-interceptor } } } @controller-interceptors))
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
    (add-action-interceptor pass-interceptor :pass-interceptor :test :show)
    (is (= pass-interceptor (first (find-action-interceptors :test :show))))
    (is (empty? (find-action-interceptors :test :hide)))
    (reset! action-interceptors initial-action-interceptors)))

(deftest test-find-controller-interceptors
  (let [initial-controller-interceptors @controller-interceptors]
    (reset! controller-interceptors {})
    (add-controller-interceptor pass-interceptor :pass-interceptor :test #{ :show })
    (is (= [pass-interceptor] (find-controller-interceptors :test :hide)))
    (is (= [] (find-controller-interceptors :test :show)))
    (reset! controller-interceptors initial-controller-interceptors)))

(deftest test-call-app-interceptor?
  (is (call-app-interceptor? { :excludes { :test #{ :hide } } } :test :show))
  (is (not (call-app-interceptor? { :excludes { :test #{ :show } } } :test :show)))
  (is (call-app-interceptor? { :excludes { :test #{} } } :test :show))
  (is (call-app-interceptor? { :excludes { :test :blah } } :test :show))
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
    [pass-interceptor pass-interceptor] 
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
  (request/set-request-map { :controller controller-name, :action action-name, :request { :method "GET" } }
    (is (run-action))))

(deftest test-call-controller
  (request/set-request-map { :controller controller-name, :action action-name, :request { :method "GET" } }
    (is (call-controller)))
  (let [initial-controller-actions @controller-actions]
    (reset! controller-actions {})
    (request/set-request-map { :controller controller-name, :action action-name, :request { :method "GET" } }
      (is (call-controller)))
    (reset! controller-actions initial-controller-actions)))