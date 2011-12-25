(ns conjure.core.controller.util
  (:require [clojure.tools.logging :as logging]
            [clojure.string :as str-utils]
            [conjure.core.config.environment :as environment]
            [conjure.core.server.request :as request]
            [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.file-utils :as file-utils]
            [clojure.tools.servlet-utils :as servlet-utils]
            [clojure.tools.string-utils :as string-utils]
            [conjure.core.util.conjure-utils :as conjure-utils]))

(def controllers-dir "controllers")
(def controllers-namespace controllers-dir)
(def controller-file-name-ending "_controller.clj")
(def controller-namespace-ending "-controller")

(def controller-actions (atom {}))

(def action-interceptors (atom {}))
(def controller-interceptors (atom {}))
(def app-interceptors (atom []))

(defn 
#^{ :doc "Finds the controller directory." }
  find-controllers-directory []
  (environment/find-in-source-dir controllers-dir))

(defn
#^{ :doc "Returns the controller file name for the given controller name." }
  controller-file-name-string [controller-name]
  (if (and controller-name (> (. controller-name length) 0))
    (str (loading-utils/dashes-to-underscores controller-name) controller-file-name-ending)))

(defn
#^{ :doc "Returns the controller file name generated from the given request map." }
  controller-file-name []
  (controller-file-name-string (request/controller)))
  
(defn
#^{ :doc "Returns the controller name for the given controller file." }
  controller-from-file [controller-file]
  (when controller-file
    (if (string? controller-file)
      (string-utils/strip-ending 
        (loading-utils/clj-file-to-symbol-string controller-file)
        controller-namespace-ending)
      (controller-from-file (.getName controller-file)))))

(defn
#^{ :doc "Finds a controller file with the given controller name." }
  find-controller-file
  ([controller-name] (find-controller-file (find-controllers-directory) controller-name)) 
  ([controllers-directory controller-name]
    (if controller-name
      (file-utils/find-file controllers-directory (controller-file-name-string controller-name)))))

(defn
#^{ :doc "Returns the controller namespace for the given controller." }
  controller-namespace [controller]
  (when controller
    (str controllers-namespace "." (loading-utils/underscores-to-dashes controller) controller-namespace-ending)))

(defn
  is-controller-namespace? [namespace]
  (when namespace
    (if (string? namespace)
      (and
        (.startsWith namespace (str controllers-dir "."))
        (not (= namespace "controllers.app")))
      (is-controller-namespace? (name (ns-name namespace))))))

(defn
#^{ :doc "Returns the controller from the given namespace. The controller is assumed to be the last part of the 
namespace." }
  controller-from-namespace [namespace]
  (when namespace
    (if (string? namespace)
      (string-utils/strip-ending 
        (last (str-utils/split namespace #"\."))
        controller-namespace-ending)
      (controller-from-namespace (name (ns-name namespace))))))

(defn
  controller-file-name? [file-name]
  (when file-name
    (.endsWith file-name "_controller.clj")))

(defn
  all-controller-file-names []
  (filter controller-file-name?
    (concat
      (loading-utils/all-class-path-file-names controllers-dir)
      (servlet-utils/all-file-names controllers-dir (request/servlet-context)))))

(defn
#^{ :doc "Returns the names of all of the controllers for this app." }
  all-controllers []
  (map controller-from-file (all-controller-file-names)))

(defn
#^{ :doc "Returns true if the given controller exists." }
  controller-exists? [controller-file-name]
  (loading-utils/namespace-exists? (controller-namespace (controller-from-file controller-file-name))))

(defn
#^{ :doc "Reloads all conjure namespaces referenced by the given controller." }
  reload-conjure-namespaces [controller]
  (conjure-utils/reload-conjure-namespaces (controller-namespace controller)))

(defn
#^{ :doc "Loads the given controller file." }
  load-controller
  ([] (load-controller (request/controller)))
  ([controller]
    (let [controller-filename (controller-file-name-string controller)]
      (when (and controller-filename (controller-exists? controller-filename))
        (require :reload (symbol (controller-namespace controller)))
        (reload-conjure-namespaces controller)))))

(defn
#^{ :doc "Returns the namespace for the given controller. This is the namespace object, not the string represenation of
the namespace. If the namespace is not found, this function attempts to reload the controller then finds the namespace
again." }
  find-controller-namespace [controller]
  (let [controller-namespace-str (controller-namespace controller)
        controller-namespace (find-ns (symbol controller-namespace-str))]
    (if controller-namespace
      controller-namespace
      (do
        (load-controller controller)
        (find-ns (symbol controller-namespace-str))))))

(defn
#^{ :doc "Returns all of the controller namespaces in the app." }
  all-controller-namespaces []
  (map find-controller-namespace (all-controllers)))

(defn
#^{ :doc "Returns fully qualified action generated from the given request map." }
  fully-qualified-action []
  (let [controller (request/controller)
        action (request/action)]
    (if (and controller action)
      (str (controller-namespace controller) "/" action))))

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
#^{ :doc "Returns the actions map for the given controller." }
  actions-map [controller]
  (when controller
    (get @controller-actions (keyword controller))))

(defn
#^{ :doc "Returns the methods map for the given controller and action." }
  methods-map [controller action]
  (when action
    (get (actions-map controller) (keyword action))))
  
(defn
#^{ :doc "Returns the action function for the given controller, action, and method. If method is not given or nil, then
the method is assumed to be :all. If no matching method is found, then nil is returned." }
  action-function 
  ([controller action] (action-function controller action nil))
  ([controller action method]
    (let [all-methods (methods-map controller action)]
      (or (get all-methods method) (get all-methods :all)))))

(defn
#^{ :doc "Returns the action function for the controller and action listed in the request-map." }
  find-action-fn []
  (action-function (request/controller) (request/action) (method-key)))

(defn
#^{ :doc "Returns all of the action method-map pairs for the given controller filtered by the given map. Both includes 
and excludes must be sets of action name keywords. If includes is given, then excludes is ignored." }
  find-actions 
  ([controller] (find-actions controller {}))
  ([controller { :keys [includes excludes], :or { includes nil, excludes #{} } }]
    (filter 
      (fn [[action methods-map]]
        (if includes
          (contains? includes action)
          (not (contains? excludes action))))
      (actions-map controller))))

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
#^{ :doc "adds the given action function into the given controllers map and returns the result." }
  assoc-controllers [controllers-map { controller :controller, :as params }]
  (let [controller-key (keyword controller)]
    (assoc controllers-map controller-key 
      (assoc-actions (get controllers-map controller-key) params))))

(defn
#^{ :doc "Adds the given action function to the list of action functions to call." }
  add-action-function [action-function params]
  (reset! controller-actions
    (assoc-controllers @controller-actions 
      (assoc params :action-function action-function))))

(defn
#^{ :doc "Copies all of the actions from the given controller." }
  copy-actions
  ([to-controller from-controller] (copy-actions to-controller from-controller {}))
  ([to-controller from-controller { :keys [includes excludes], :or { includes nil, excludes #{} }, :as filter-map }]
    (doseq [[action methods-map] (find-actions from-controller filter-map)]
      (doseq [[action-fn methods] (action-fn-method-map methods-map)]
        (add-action-function action-fn 
          { :action action, :controller to-controller, :methods methods })))))

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
#^{ :doc "Adds the given action interceptor to the given controller interceptor map." }
  assoc-action-interceptors [controller-interceptor-map action-interceptor interceptor-name action]
  (let [action-key (keyword action)]
    (assoc controller-interceptor-map action-key
      (assoc-name-interceptors (get controller-interceptor-map action-key) action-interceptor interceptor-name))))

(defn
#^{ :doc "Adds the given action interceptor to the given action interceptor map." }
  assoc-controller-interceptors [action-interceptor-map action-interceptor interceptor-name controller action]
  (let [controller-key (keyword controller)]
    (assoc action-interceptor-map controller-key
      (assoc-action-interceptors (get action-interceptor-map controller-key) action-interceptor interceptor-name action))))

(defn
#^{ :doc "Adds the given action interceptor to the list of action interceptors to call." }
  add-action-interceptor [action-interceptor interceptor-name controller action]
  (reset! action-interceptors
    (assoc-controller-interceptors @action-interceptors action-interceptor interceptor-name controller action)))

(defn
#^{ :doc "Adds the given excludes interceptor map with the given controller interceptor and excludes set." }
  update-exclude-interceptor-list [exclude-interceptor-map controller-interceptor interceptor-name excludes]
  (let [exclude-map { :interceptor controller-interceptor }]
    (assoc exclude-interceptor-map (keyword interceptor-name)
      (if excludes 
        (assoc exclude-map :excludes excludes) 
        exclude-map))))

(defn
#^{ :doc "Adds the given controller interceptor to the given controller interceptor map." }
  assoc-controller-excludes-interceptors [controller-interceptor-map controller-interceptor interceptor-name controller excludes]
  (let [controller-key (keyword controller)]
    (assoc controller-interceptor-map controller-key
      (update-exclude-interceptor-list (get controller-interceptor-map controller-key) controller-interceptor
        interceptor-name excludes))))

(defn
#^{ :doc "Adds the given controller interceptor to the list of controller interceptors to call, excluding the 
interceptor if any of the actions in excludes is called.." }
  add-controller-interceptor [controller-interceptor interceptor-name controller excludes]
  (reset! controller-interceptors
    (assoc-controller-excludes-interceptors @controller-interceptors controller-interceptor interceptor-name controller
      excludes)))

(defn
#^{ :doc "Adds the given interceptor to the given controller, including or excluding the given actions. Note, adding
includes will completely ignore excludes." }
  add-interceptor [interceptor interceptor-name controller excludes includes]
  (when (and interceptor controller)
    (if (and includes (not-empty includes))
      (doseq [include-action includes]
        (add-action-interceptor interceptor interceptor-name controller include-action))
      (add-controller-interceptor interceptor interceptor-name controller excludes))))

(defn
#^{ :doc "Creates the interceptor map for the given app interceptor." }
  app-interceptor-map [interceptor excludes]
  { :interceptor interceptor, :excludes excludes })

(defn
#^{ :doc "Adds the given interceptor and excludes map to the given list of app interceptors." }
  add-app-interceptor-to-list [app-interceptors interceptor excludes]
  (cons (app-interceptor-map interceptor excludes) app-interceptors))

(defn
#^{ :doc "Adds the given interceptor to the list of app interceptors, including or excluding the given controllers and 
actions." }
  add-app-interceptor [interceptor excludes]
  (reset! app-interceptors
    (add-app-interceptor-to-list @app-interceptors interceptor excludes)))

(defn
#^{ :doc "Returns the action interceptor for the given controller and action." }
  find-action-interceptors [controller action]
  (vals (get (get @action-interceptors (keyword controller)) (keyword action))))

(defn
#^{ :doc "Returns the controller interceptors for the given controller and action." }
  find-controller-interceptors [controller action]
  (let [action-key (keyword action)]
    (filter identity 
      (map 
        (fn [exclude-interceptor-map]
          (let [excludes (:excludes exclude-interceptor-map)]
            (when (not (and excludes (contains? excludes action-key)))
              (:interceptor exclude-interceptor-map)))) 
        (vals (get @controller-interceptors (keyword controller)))))))

(defn
#^{ :doc "Returns true if the app interceptor in the given interceptor map should be called for the given controller 
and action." }
  call-app-interceptor? [interceptor-map controller action]
  (let [action-set (get (:excludes interceptor-map) (keyword controller))]
    (if (and action-set (set? action-set) (not-empty action-set))
      (not (contains? action-set (keyword action)))
      true)))

(defn
#^{ :doc "Returns the app interceptors in app-interceptors to call for the given controller and action." }
  valid-app-interceptors [app-interceptors controller action]
  (map :interceptor
    (filter #(call-app-interceptor? % controller action) app-interceptors)))

(defn
#^{ :doc "Returns the app interceptors to call for the given controller and action." }
  find-app-interceptors [controller action]
  (valid-app-interceptors @app-interceptors controller action))

(defn
#^{ :doc "Returns a single interceptor created by chaining all of the interceptors which apply to the given controller
and action. If no interceptors apply to the given controller and action, a simple pass through interceptor is created." }
  create-interceptor-chain [controller action]
  (or
    (apply chain-interceptors
      (concat
        (find-action-interceptors controller action)
        (find-controller-interceptors controller action)
        (find-app-interceptors controller action)))
    (fn [action-fn]
      (action-fn))))

(defn
#^{ :doc "Runs all interceptors passing the given action function. If there are no interceptors for the
given action and controller, then this function simply runs the action function." }
  run-interceptors [action-fn]
  ((create-interceptor-chain (request/controller) (request/action)) action-fn))

(defn
#^{ :doc "Attempts to run the action requested in the request-map. If the action is successful, its response is 
returned, otherwise nil is returned." }
  run-action []
  (let [action-fn (find-action-fn)]
    (when action-fn
      (logging/debug (str "Running action: " (fully-qualified-action)))
      (run-interceptors action-fn))))

(defn
#^{ :doc "Calls the given controller with the given request map returning the response." }
  call-controller []
  (if (environment/reload-files?)
    (do 
      (load-controller)
      (run-action))
    (or 
      (run-action)
      (do
        (load-controller)
        (run-action)))))

(defn
#^{ :doc "Calls the given action in the given controller. This is a convenience function over wrapping call-controller
in request/with-controller-action calls." }
  call-controller-action
  ([controller action]
    (request/with-controller-action controller action
      (call-controller)))
  ([controller action params]
    (request/with-parameters params
      (call-controller-action controller action))))