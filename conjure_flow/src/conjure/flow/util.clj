(ns conjure.flow.util
  (:require [conjure.config.environment :as environment]
            [clojure.string :as str-utils]
            [clojure.tools.file-utils :as file-utils]
            [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.logging :as logging]
            [clojure.tools.servlet-utils :as servlet-utils]
            [clojure.tools.string-utils :as string-utils]
            [conjure.util.conjure-utils :as conjure-utils]
            [conjure.util.request :as request]))

(def flows-dir "flows")
(def flows-namespace flows-dir)
(def flow-file-name-ending "_flow.clj")
(def flow-namespace-ending "-flow")

(def flow-actions (atom {}))

(def action-interceptors (atom {}))
(def service-interceptors (atom {}))
(def app-interceptors (atom []))

(defn 
#^{ :doc "Finds the flow directory." }
  find-flows-directory []
  (environment/find-in-source-dir flows-dir))

(defn
#^{ :doc "Returns the flow file name for the given service." }
  flow-file-name-string [service]
  (if (and service (not-empty service))
    (str (loading-utils/dashes-to-underscores service) flow-file-name-ending)))

(defn
#^{ :doc "Returns the flow file name generated from the given request map." }
  flow-file-name []
  (flow-file-name-string (request/service)))
  
(defn
#^{ :doc "Returns the service name for the given flow file." }
  service-from-file [flow-file]
  (when flow-file
    (if (string? flow-file)
      (string-utils/strip-ending  (loading-utils/clj-file-to-symbol-string flow-file) flow-namespace-ending)
      (service-from-file (.getName flow-file)))))

(defn
#^{ :doc "Finds a flow file for the given service." }
  find-flow-file
  ([service] (find-flow-file (find-flows-directory) service)) 
  ([flows-directory service]
    (when service
      (file-utils/find-file flows-directory (flow-file-name-string service)))))

(defn
#^{ :doc "Returns the flow namespace for the given service." }
  flow-namespace [service]
  (when service
    (str flows-namespace "." (loading-utils/underscores-to-dashes service) flow-namespace-ending)))

(defn
  is-flow-namespace? [namespace]
  (when namespace
    (if (string? namespace)
      (and
        (.startsWith namespace (str flows-dir "."))
        (not (= namespace "flows.app")))
      (is-flow-namespace? (name (ns-name namespace))))))

(defn
#^{ :doc "Returns the service from the given namespace. The service is assumed to be the last part of the 
namespace." }
  service-from-namespace [namespace]
  (when namespace
    (if (string? namespace)
      (string-utils/strip-ending 
        (last (str-utils/split namespace #"\."))
        flow-namespace-ending)
      (service-from-namespace (name (ns-name namespace))))))

(defn
  flow-file-name? [file-name]
  (when file-name
    (.endsWith file-name "_flow.clj")))

(defn
  all-flow-file-names []
  (filter flow-file-name?
    (concat
      (loading-utils/all-class-path-file-names flows-dir)
      (servlet-utils/all-file-names flows-dir (request/servlet-context)))))

(defn
#^{ :doc "Returns the services of all of the flows for this app." }
  all-services []
  (map service-from-file (all-flow-file-names)))

(defn
#^{ :doc "Returns true if the given flow namespace exists." }
  flow-exists? [flow-file-name]
  (let [namespace (flow-namespace (service-from-file flow-file-name))]
    (try
      (require (symbol namespace))
      (loading-utils/namespace-exists? namespace)
      (catch java.io.FileNotFoundException e
        ; Ignore exception. The file could not be found.
        false))))

(defn
#^{ :doc "Reloads all conjure namespaces referenced by the given service." }
  reload-conjure-namespaces [service]
  (conjure-utils/reload-conjure-namespaces (flow-namespace service)))

(defn
#^{ :doc "Loads the flow file for the given service. The service returned by (request/service) is used if no service is
given." }
  load-flow
  ([] (load-flow (request/service)))
  ([service]
    (when-let [flow-filename (flow-file-name-string service)]
      (when (flow-exists? flow-filename)
        (require :reload (symbol (flow-namespace service)))
        (reload-conjure-namespaces service)))))

(defn
#^{ :doc "Returns the namespace for the given flow. This is the namespace object, not the string represenation of
the namespace. If the namespace is not found, this function attempts to reload the flow then finds the namespace
again." }
  find-flow-namespace [service]
  (let [flow-namespace-str (flow-namespace service)]
    (if-let [flow-namespace (find-ns (symbol flow-namespace-str))]
      flow-namespace
      (do
        (load-flow service)
        (find-ns (symbol flow-namespace-str))))))

(defn
#^{ :doc "Returns all of the flow namespaces in the app." }
  all-flow-namespaces []
  (map find-flow-namespace (all-services)))

(defn
#^{ :doc "Returns fully qualified action generated from the given request map." }
  fully-qualified-action []
  (when-let [service (request/service)]
    (when-let [action (request/action)]
      (str (flow-namespace service) "/" action))))

(defn
#^{ :doc "Returns a keyword for the request method." }
  method-key []
  (let [request-method (request/method)]
    (cond
      (= "GET" request-method) :get
      (= "POST" request-method) :post
      (= "PUT" request-method) :put
      (= "DELETE" request-method) :delete)))

(defn
#^{ :doc "Returns the actions map for the given service." }
  actions-map [service]
  (when service
    (get @flow-actions (keyword service))))

(defn
#^{ :doc "Returns the methods map for the given service and action." }
  methods-map [service action]
  (when action
    (get (actions-map service) (keyword action))))
  
(defn
#^{ :doc "Returns the action function for the given service, action, and method. If method is not given or nil, then
the method is assumed to be :all. If no matching method is found, then nil is returned." }
  action-function 
  ([service action] (action-function service action nil))
  ([service action method]
    (let [all-methods (methods-map service action)]
      (or (get all-methods method) (get all-methods :all)))))

(defn
#^{ :doc "Returns the action function for the service and action listed in the request-map." }
  find-action-fn []
  (action-function (request/service) (request/action) (method-key)))

(defn
#^{ :doc "Returns all of the action method-map pairs for the given service filtered by the given map. Both includes 
and excludes must be sets of action name keywords. If includes is given, then excludes is ignored." }
  find-actions 
  ([service] (find-actions service {}))
  ([service { :keys [includes excludes], :or { includes nil, excludes #{} } }]
    (filter 
      (fn [[action methods-map]]
        (if includes
          (contains? includes action)
          (not (contains? excludes action))))
      (actions-map service))))

(defn
#^{ :doc "Returns a map from all the action functions in the given methods-map to the associated methods." }
  action-fn-method-map [methods-map]
  (reduce 
    (fn [output [method action-fn]]
      (assoc output action-fn (conj (or (get output action-fn) []) method)))
    {} 
    methods-map))

(defn 
#^{ :doc "adds the given action function into the given methods map and returns the result." }
  assoc-methods [methods-map { action-function :action-function, methods :methods, :or { methods [:all] } }]
  (reduce #(assoc %1 %2 action-function) methods-map methods))
  
(defn
#^{ :doc "adds the given action function into the given actions map and returns the result." }
  assoc-actions [actions-map { action :action, :as params }]
  (let [action-key (keyword action)]
    (assoc actions-map action-key 
      (assoc-methods (get actions-map action-key) params))))
  
(defn
#^{ :doc "adds the given action function into the given services map and returns the result." }
  assoc-services [services-map { service :service, :as params }]
  (let [service-key (keyword service)]
    (assoc services-map service-key 
      (assoc-actions (get services-map service-key) params))))

(defn
#^{ :doc "Adds the given action function to the list of action functions to call." }
  add-action-function [action-function params]
  (reset! flow-actions
    (assoc-services @flow-actions 
      (assoc params :action-function action-function))))

(defn
#^{ :doc "Copies all of the actions from the given service." }
  copy-actions
  ([to-service from-service] (copy-actions to-service from-service {}))
  ([to-service from-service { :keys [includes excludes], :or { includes nil, excludes #{} }, :as filter-map }]
    (doseq [[action methods-map] (find-actions from-service filter-map)]
      (doseq [[action-fn methods] (action-fn-method-map methods-map)]
        (add-action-function action-fn 
          { :action action, :service to-service, :methods methods })))))

(defn
#^{ :doc "If the given interceptor is a function, then this function returns it. Otherwise, this function throws an 
exception showing what the interceptor is." }
  check-inteceptor-fn? [interceptor interceptor-name]
  (if (or (nil? interceptor) (fn? interceptor))
    interceptor
    (throw (new RuntimeException (str interceptor-name " is not a function. " interceptor-name ": " interceptor)))))

(defn
#^{ :doc "Chains all of the given interceptors together. If any interceptor is nil, it is simply ignored. If all 
interceptors are nil, then this function returns nil." }
  chain-interceptors
  ([] nil)
  ([interceptor] interceptor) 
  ([parent-interceptor child-interceptor]
    (if parent-interceptor
      (if child-interceptor
        (do
          (check-inteceptor-fn? parent-interceptor "parent-interceptor")
          (check-inteceptor-fn? child-interceptor "child-interceptor")
          (fn [action-fn] 
            (parent-interceptor #(child-interceptor action-fn))))
        (check-inteceptor-fn? parent-interceptor "parent-interceptor"))
      (check-inteceptor-fn? child-interceptor "child-interceptor")))
  ([parent-interceptor child-interceptor & more]
    (reduce chain-interceptors (chain-interceptors parent-interceptor child-interceptor) more)))

(defn
#^{ :doc "Adds the given action interceptor to the given action interceptor map." }
  assoc-name-interceptors [action-interceptor-map action-interceptor interceptor-name]
  (let [name-key (keyword interceptor-name)]
    (assoc action-interceptor-map name-key action-interceptor)))

(defn
#^{ :doc "Adds the given action interceptor to the given service interceptor map." }
  assoc-action-interceptors [service-interceptor-map action-interceptor interceptor-name action]
  (let [action-key (keyword action)]
    (assoc service-interceptor-map action-key
      (assoc-name-interceptors (get service-interceptor-map action-key) action-interceptor interceptor-name))))

(defn
#^{ :doc "Adds the given action interceptor to the given action interceptor map." }
  assoc-service-interceptors [action-interceptor-map action-interceptor interceptor-name service action]
  (let [service-key (keyword service)]
    (assoc action-interceptor-map service-key
      (assoc-action-interceptors (get action-interceptor-map service-key) action-interceptor interceptor-name action))))

(defn
#^{ :doc "Adds the given action interceptor to the list of action interceptors to call." }
  add-action-interceptor [action-interceptor interceptor-name service action]
  (reset! action-interceptors
    (assoc-service-interceptors @action-interceptors action-interceptor interceptor-name service action)))

(defn
#^{ :doc "Adds the given excludes interceptor map with the given service interceptor and excludes set." }
  update-exclude-interceptor-list [exclude-interceptor-map service-interceptor interceptor-name excludes]
  (let [exclude-map { :interceptor service-interceptor }]
    (assoc exclude-interceptor-map (keyword interceptor-name)
      (if excludes 
        (assoc exclude-map :excludes excludes) 
        exclude-map))))

(defn
#^{ :doc "Adds the given service interceptor to the given service interceptor map." }
  assoc-service-excludes-interceptors [service-interceptor-map service-interceptor interceptor-name service excludes]
  (let [service-key (keyword service)]
    (assoc service-interceptor-map service-key
      (update-exclude-interceptor-list (get service-interceptor-map service-key) service-interceptor
        interceptor-name excludes))))

(defn
#^{ :doc "Adds the given service interceptor to the list of service interceptors to call, excluding the 
interceptor if any of the actions in excludes is called.." }
  add-service-interceptor [service-interceptor interceptor-name service excludes]
  (reset! service-interceptors
    (assoc-service-excludes-interceptors @service-interceptors service-interceptor interceptor-name service
      excludes)))

(defn
#^{ :doc "Adds the given interceptor to the given service, including or excluding the given actions. Note, adding
includes will completely ignore excludes." }
  add-interceptor [interceptor interceptor-name service excludes includes]
  (when (and interceptor service)
    (if (and includes (not-empty includes))
      (doseq [include-action includes]
        (add-action-interceptor interceptor interceptor-name service include-action))
      (add-service-interceptor interceptor interceptor-name service excludes))))

(defn
#^{ :doc "Creates the interceptor map for the given app interceptor." }
  app-interceptor-map [interceptor excludes]
  { :interceptor interceptor, :excludes excludes })

(defn
#^{ :doc "Adds the given interceptor and excludes map to the given list of app interceptors." }
  add-app-interceptor-to-list [app-interceptors interceptor excludes]
  (cons (app-interceptor-map interceptor excludes) app-interceptors))

(defn
#^{ :doc "Adds the given interceptor to the list of app interceptors, including or excluding the given services and 
actions." }
  add-app-interceptor [interceptor excludes]
  (reset! app-interceptors
    (add-app-interceptor-to-list @app-interceptors interceptor excludes)))

(defn
#^{ :doc "Returns the action interceptor for the given service and action." }
  find-action-interceptors [service action]
  (vals (get (get @action-interceptors (keyword service)) (keyword action))))

(defn
#^{ :doc "Returns the service interceptors for the given service and action." }
  find-service-interceptors [service action]
  (let [action-key (keyword action)]
    (filter identity 
      (map 
        (fn [exclude-interceptor-map]
          (let [excludes (:excludes exclude-interceptor-map)]
            (when (not (and excludes (contains? excludes action-key)))
              (:interceptor exclude-interceptor-map)))) 
        (vals (get @service-interceptors (keyword service)))))))

(defn
#^{ :doc "Returns true if the app interceptor in the given interceptor map should be called for the given service 
and action." }
  call-app-interceptor? [interceptor-map service action]
  (let [action-set (get (:excludes interceptor-map) (keyword service))]
    (if (and action-set (set? action-set) (not-empty action-set))
      (not (contains? action-set (keyword action)))
      true)))

(defn
#^{ :doc "Returns the app interceptors in app-interceptors to call for the given service and action." }
  valid-app-interceptors [app-interceptors service action]
  (map :interceptor
    (filter #(call-app-interceptor? % service action) app-interceptors)))

(defn
#^{ :doc "Returns the app interceptors to call for the given service and action." }
  find-app-interceptors [service action]
  (valid-app-interceptors @app-interceptors service action))

(defn
#^{ :doc "Returns a single interceptor created by chaining all of the interceptors which apply to the given service
and action. If no interceptors apply to the given service and action, a simple pass through interceptor is created." }
  create-interceptor-chain [service action]
  (or
    (apply chain-interceptors
      (concat
        (find-action-interceptors service action)
        (find-service-interceptors service action)
        (find-app-interceptors service action)))
    (fn [action-fn]
      (action-fn))))

(defn
#^{ :doc "Runs all interceptors passing the given action function. If there are no interceptors for the
given action and service, then this function simply runs the action function." }
  run-interceptors [action-fn]
  ((create-interceptor-chain (request/service) (request/action)) action-fn))

(defn
#^{ :doc "Attempts to run the action requested in the request-map. If the action is successful, its response is 
returned, otherwise nil is returned." }
  run-action []
  (when-let [action-fn (find-action-fn)]
    (run-interceptors action-fn)))

(defn
#^{ :doc "Calls the given service with the given request map returning the response." }
  call-flow []
  (if (environment/reload-files?)
    (do 
      (load-flow)
      (run-action))
    (or 
      (run-action)
      (do
        (load-flow)
        (run-action)))))

(defn
#^{ :doc "Calls the given action in the given service. This is a convenience function over wrapping call-flow
in request/with-service-action calls." }
  call-flow-action
  ([service action]
    (request/with-service-action service action
      (call-flow)))
  ([service action params]
    (request/with-parameters params
      (call-flow-action service action))))