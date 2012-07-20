(ns conjure.flow.test-util
  (:import [java.io File])
  (:use clojure.test
        conjure.flow.util
        test-helper)
  (:require [clojure.tools.logging :as logging]
            [conjure.util.request :as request]))

(def service-name "test")
(def action-name "show")

(use-fixtures :once init-server)
  
(deftest test-find-flows-directory
  (let [flows-directory (find-flows-directory)]
    (is (not (nil? flows-directory)))
    (is (instance? File flows-directory))))

(deftest test-find-flows-directory
  (let [flows-directory (find-flows-directory)]
    (is (not (nil? flows-directory)))
    (is (instance? File flows-directory))))
    
(deftest test-flow-file-name-string
  (let [flow-file-name (flow-file-name-string service-name)]
    (is (not (nil? flow-file-name)))
    (is (= "test_flow.clj" flow-file-name)))
  (let [flow-file-name (flow-file-name-string "test-name")]
    (is (not (nil? flow-file-name)))
    (is (= "test_name_flow.clj" flow-file-name)))
  (is (nil? (flow-file-name-string nil)))
  (is (nil? (flow-file-name-string ""))))

(deftest test-flow-file-name
  (request/set-request-map { :service service-name }
    (is (= (str service-name "_flow.clj") (flow-file-name))))
  (request/set-request-map { :service "" }
    (is (nil? (flow-file-name))))
  (request/set-request-map { :service nil }
    (is (nil? (flow-file-name))))
  (is (nil? (flow-file-name))))
  
(deftest test-service-from-file
  (let [flow-file (new File "test_flow.clj")
        service-name (service-from-file flow-file)]
    (is (not (nil? service-name)))
    (is (= "test" service-name)))
  (let [flow-file (new File "test_name_flow.clj")
        service-name (service-from-file flow-file)]
    (is (not (nil? service-name)))
    (is (= "test-name" service-name)))
  (is (nil? (service-from-file nil))))
  
(deftest test-find-flow-file
  (let [flows-directory (find-flows-directory)]
    (let [flow-file (find-flow-file flows-directory service-name)]
      (is (not (nil? flow-file))))
    (let [flow-file (find-flow-file flows-directory "fail")]
      (is (nil? flow-file)))
    (let [flow-file (find-flow-file flows-directory nil)]
      (is (nil? flow-file)))
  (let [flow-file (find-flow-file "test")]
    (is (not (nil? flow-file))))))
    
(deftest test-flow-namespace
  (let [flow-ns (flow-namespace service-name)]
    (is (not (nil? flow-ns)))
    (is (= "flows.test-flow" flow-ns)))
  (let [flow-ns (flow-namespace "test-name")]
    (is (not (nil? flow-ns)))
    (is (= "flows.test-name-flow" flow-ns)))
  (let [flow-ns (flow-namespace "test_name")]
    (is (not (nil? flow-ns)))
    (is (= "flows.test-name-flow" flow-ns)))
  (is (nil? (flow-namespace nil))))

(deftest test-is-flow-namespace?
  (is (is-flow-namespace? "flows.test-name-flow"))
  (is (not (is-flow-namespace? "fail")))
  (is (not (is-flow-namespace? "")))
  (is (not (is-flow-namespace? nil))))

(defn find-first-str [string str-seq]
  (some #(when (= string %1) %1) str-seq))

(deftest test-all-flow-namespaces
  (let [flow-namespaces (all-flow-namespaces)]
    (is (= 1 (count flow-namespaces)))
    (is (find-first-str "flows.test-flow"
          (map #(name (ns-name %)) flow-namespaces)))))

(deftest test-service-from-namespace
  (is (= "test" (service-from-namespace "flows.test-flow")))
  (is (nil? (service-from-namespace nil))))

(deftest test-all-services
  (let [services (all-services)]
    (is (= 1 (count services)))
    (is (find-first-str "test" services))))

(deftest test-flow-exists?
  (request/set-request-map { :service service-name }
    (is (flow-exists? (flow-file-name))))
  (request/set-request-map { :service "fail" }
    (is (not (flow-exists? (flow-file-name))))))

(deftest test-load-flow
  (load-flow service-name))

(deftest test-fully-qualified-action
  (request/set-request-map { :service service-name, :action action-name }
    (is (= (str "flows." service-name "-flow/" action-name) (fully-qualified-action))))
  (request/set-request-map { :service service-name }
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
  (is (actions-map service-name))
  (is (nil? (actions-map "fail")))
  (is (nil? (actions-map nil))))

(deftest test-methods-map
  (is (methods-map service-name action-name))
  (is (nil? (methods-map "fail" action-name)))
  (is (nil? (methods-map nil action-name)))
  (is (nil? (methods-map service-name "fail"))))

(deftest test-action-function
  (is (action-function service-name action-name :all))
  (is (action-function service-name action-name))
  (is (action-function service-name action-name :default))
  (is (nil? (action-function service-name "fail" :all)))
  (is (nil? (action-function "fail" action-name :all))))

(deftest test-find-action-fn
  (request/set-request-map { :service service-name, :action action-name, :request { :method "GET" } }
    (is (find-action-fn))))

(deftest test-find-actions
  (is (not-empty (find-actions service-name)))
  (is (not-empty (find-actions service-name { :includes #{ (keyword action-name) } })))
  (is (not-empty (find-actions service-name { :excludes #{ :fail } })))
  (is (empty? (find-actions service-name { :excludes #{ (keyword action-name) } })))
  (is (empty? (find-actions service-name { :includes #{ :fail } })))
  (is (empty? (find-actions service-name { :includes #{ :fail }, :excludes #{ :fail } })))
  (is (not-empty (find-actions service-name 
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

(deftest test-assoc-services
  (let [test-action (fn [request-map] nil)
        params { :action action-name, :action-function test-action, :methods [:all] }
        action-map { (keyword action-name) { :all test-action } }]
    (is (= 
      { (keyword service-name) action-map } 
      (assoc-services {} (assoc params :service service-name))))
    (is (= 
      { (keyword service-name) action-map, :foo action-map } 
      (assoc-services { :foo action-map } (assoc params :service service-name))))))

(deftest test-add-action-function
  (let [initial-flow-actions @flow-actions
        test-action (fn [request-map] nil)
        params { :service service-name, :action action-name }
        service-map { (keyword service-name) { (keyword action-name) { :all test-action } } }] 
    (reset! flow-actions {})
    (add-action-function test-action (assoc params :methods [:all]))
    (is (= service-map @flow-actions))
    (reset! flow-actions {})
    (add-action-function test-action params)
    (is (= service-map @flow-actions))
    (reset! flow-actions initial-flow-actions)))

(deftest test-copy-actions
  (let [initial-flow-actions @flow-actions
        params { :service service-name, :action action-name }]
    (reset! flow-actions {})
    (add-action-function test-action (assoc params :methods [:all]))
    (copy-actions "test2" "test")
    (is (= { (keyword action-name) { :all test-action } } (:test2 @flow-actions)))
    (reset! flow-actions {})
    
    (add-action-function test-action (assoc params :methods [:all]))
    (copy-actions "test2" "test" { :includes #{ (keyword action-name) } })
    (is (= { (keyword action-name) { :all test-action } } (:test2 @flow-actions)))
    (reset! flow-actions initial-flow-actions)))

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

(deftest test-assoc-service-interceptors
  (is (= { :test { :show { :pass-interceptor pass-interceptor } } } 
        (assoc-service-interceptors {} pass-interceptor :pass-interceptor :test :show)))
  (is (= { :test { :show { :pass-interceptor pass-interceptor }, :hide { :pass-interceptor pass-interceptor } } } 
        (assoc-service-interceptors { :test { :hide { :pass-interceptor pass-interceptor } } } 
          pass-interceptor :pass-interceptor :test :show)))
  (let [interceptor-one (create-stack-interceptor "one")
        interceptor-two (create-stack-interceptor "two")]
    (is (= { :test { :show { :one interceptor-one, :two interceptor-two } } }
          (assoc-service-interceptors { :test { :show { :two interceptor-two } } } 
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

(deftest test-assoc-service-excludes-interceptors
  (is (= { :test { :pass-interceptor { :interceptor pass-interceptor } } }
    (assoc-service-excludes-interceptors {} pass-interceptor :pass-interceptor :test nil)))
  (is (= { :test { :pass-interceptor { :interceptor pass-interceptor } } }
    (assoc-service-excludes-interceptors { :test { :pass-interceptor { :interceptor pass-interceptor } } }
      pass-interceptor :pass-interceptor :test nil)))
  (is (= { :test { :pass-interceptor { :interceptor pass-interceptor } 
                   :pass-interceptor2 { :interceptor pass-interceptor } } }
    (assoc-service-excludes-interceptors 
      { :test 
        { :pass-interceptor { :interceptor pass-interceptor } 
          :pass-interceptor2 { :interceptor pass-interceptor } } }
      pass-interceptor :pass-interceptor2 :test nil))))

(deftest test-add-service-interceptor
  (let [initial-service-interceptors @service-interceptors]
    (reset! service-interceptors {})
    (add-service-interceptor pass-interceptor :pass-interceptor :test #{ :show })
    (is (= { :test { :pass-interceptor { :excludes #{ :show }, :interceptor pass-interceptor } } }
          @service-interceptors))
    (reset! service-interceptors initial-service-interceptors)))

(deftest test-add-interceptor
  (let [initial-action-interceptors @action-interceptors
        initial-service-interceptors @service-interceptors]
    (reset! action-interceptors {})
    (reset! service-interceptors {})
    (add-interceptor pass-interceptor :pass-interceptor :test nil [ :show ])
    (is (= { :test { :show { :pass-interceptor pass-interceptor } } } @action-interceptors))
    (is (= {} @service-interceptors))
    (reset! action-interceptors {})
    (add-interceptor pass-interceptor :pass-interceptor :test #{ :show } nil)
    (is (= {} @action-interceptors))
    (is (= { :test { :pass-interceptor { :excludes #{ :show }, :interceptor pass-interceptor } } } @service-interceptors))
    (reset! action-interceptors initial-action-interceptors)
    (reset! service-interceptors initial-service-interceptors)))

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

(deftest test-find-service-interceptors
  (let [initial-service-interceptors @service-interceptors]
    (reset! service-interceptors {})
    (add-service-interceptor pass-interceptor :pass-interceptor :test #{ :show })
    (is (= [pass-interceptor] (find-service-interceptors :test :hide)))
    (is (= [] (find-service-interceptors :test :show)))
    (reset! service-interceptors initial-service-interceptors)))

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
  (request/set-request-map { :service service-name, :action action-name, :request { :method "GET" } }
    (is (run-action))))

(deftest test-call-flow
  (request/set-request-map { :service service-name, :action action-name, :request { :method "GET" } }
    (is (call-flow)))
  (let [initial-flow-actions @flow-actions]
    (reset! flow-actions {})
    (request/set-request-map { :service service-name, :action action-name, :request { :method "GET" } }
      (is (call-flow)))
    (reset! flow-actions initial-flow-actions)))