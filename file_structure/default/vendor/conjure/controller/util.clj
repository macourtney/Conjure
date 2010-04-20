(ns conjure.controller.util
  (:require [clojure.contrib.logging :as logging]
            [clojure.contrib.seq-utils :as seq-utils]
            [clojure.contrib.str-utils :as str-utils]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.string-utils :as string-utils]
            environment))

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
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith controllers-dir))
    (. (loading-utils/get-classpath-dir-ending-with "app") listFiles)))

(defn
#^{ :doc "Returns the controller file name for the given controller name." }
  controller-file-name-string [controller-name]
  (if (and controller-name (> (. controller-name length) 0))
    (str (loading-utils/dashes-to-underscores controller-name) controller-file-name-ending)))

(defn
#^{ :doc "Returns the controller file name generated from the given request map." }
  controller-file-name [request-map]
  (controller-file-name-string (:controller request-map)))
  
(defn
#^{ :doc "Returns the controller name for the given controller file." }
  controller-from-file [controller-file]
  (if controller-file
    (let [file-to-symbol (loading-utils/clj-file-to-symbol-string (. controller-file getName))]
      (string-utils/strip-ending file-to-symbol controller-namespace-ending))))

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
  (if controller
    (str controllers-namespace "." (loading-utils/underscores-to-dashes controller) controller-namespace-ending)))
    
(defn
#^{ :doc "Returns the names of all of the controllers for this app." }
  all-controllers []
  (map controller-from-file 
    (filter 
      #(let [file-name (. % getName)] 
        (and 
          (not (= file-name "app_controller.clj")) 
          (. file-name endsWith controller-file-name-ending))) 
      (. (find-controllers-directory) listFiles))))

(defn
  all-controller-namespaces []
  (map #(symbol (controller-namespace %)) (all-controllers)))

(defn
#^{ :doc "Returns true if the given controller exists." }
  controller-exists? [controller-file-name]
  (loading-utils/resource-exists? (str controllers-dir "/" controller-file-name)))

(defn
#^{ :doc "Reloads all conjure namespaces referenced by the given controller." }
  reload-conjure-namespaces [controller]
  (loading-utils/reload-conjure-namespaces (controller-namespace controller)))

(defn
#^{ :doc "Loads the given controller file." }
  load-controller [controller]
  (let [controller-filename (controller-file-name-string controller)]
    (when (and controller-filename (controller-exists? controller-filename))
      (require :reload (symbol (controller-namespace controller)))
      (reload-conjure-namespaces controller))))

(defn
#^{ :doc "Returns fully qualified action generated from the given request map." }
  fully-qualified-action [request-map]
  (if request-map
    (let [controller (:controller request-map)
          action (:action request-map)]
      (if (and controller action)
        (str (controller-namespace controller) "/" action)))))
(defn
#^{ :doc "Returns a keyword for the request method." }
  method-key [request-map]
  (let [request-method (:method (:request request-map))]
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
#^{ :doc "Returns the action function for the controller and action listed in the given request-map." }
  find-action-fn [{ controller :controller, action :action, :as request-map }]
  (action-function controller action (method-key request-map)))

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
#^{ :doc "Returns the controller from the given namespace. The controller is assumed to be the last part of the 
namespace." }
  controller-from-namespace [namespace-name]
  (string-utils/strip-ending 
    (last (str-utils/re-split #"\." namespace-name)) controller-namespace-ending))

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
  ([interceptor] interceptor) 
  ([parent-interceptor child-interceptor]
    (if parent-interceptor
      (if child-interceptor
        (do
          (check-inteceptor-fn? parent-interceptor "parent-interceptor")
          (check-inteceptor-fn? child-interceptor "child-interceptor")
          (fn [request-map action-fn] 
            (parent-interceptor request-map #(child-interceptor %1 action-fn))))
        (check-inteceptor-fn? parent-interceptor "parent-interceptor"))
      (check-inteceptor-fn? child-interceptor "child-interceptor")))
  ([parent-interceptor child-interceptor & more]
    (reduce chain-interceptors (chain-interceptors parent-interceptor child-interceptor) more)))

(defn
#^{ :doc "Adds the given action interceptor to the given controller interceptor map." }
  assoc-action-interceptors [controller-interceptor-map action-interceptor action]
  (let [action-key (keyword action)]
    (assoc controller-interceptor-map action-key
      (chain-interceptors action-interceptor (get controller-interceptor-map action-key)))))

(defn
#^{ :doc "Adds the given action interceptor to the given action interceptor map." }
  assoc-controller-interceptors [action-interceptor-map action-interceptor controller action]
  (let [controller-key (keyword controller)]
    (assoc action-interceptor-map controller-key
      (assoc-action-interceptors (get action-interceptor-map controller-key) action-interceptor action))))

(defn
#^{ :doc "Adds the given action interceptor to the list of action interceptors to call." }
  add-action-interceptor [action-interceptor controller action]
  (reset! action-interceptors
    (assoc-controller-interceptors @action-interceptors action-interceptor controller action)))

(defn
#^{ :doc "Adds the given excludes interceptor map with the given controller interceptor and excludes set." }
  update-exclude-interceptor-list [exclude-interceptor-list controller-interceptor excludes]
  (let [exclude-map { :interceptor controller-interceptor }]
    (cons 
      (if excludes 
        (assoc exclude-map :excludes excludes) 
        exclude-map)
      exclude-interceptor-list)))

(defn
#^{ :doc "Adds the given controller interceptor to the given controller interceptor map." }
  assoc-controller-excludes-interceptors [controller-interceptor-map controller-interceptor controller excludes]
  (let [controller-key (keyword controller)]
    (assoc controller-interceptor-map controller-key
      (update-exclude-interceptor-list (get controller-interceptor-map controller-key) controller-interceptor excludes))))

(defn
#^{ :doc "Adds the given controller interceptor to the list of controller interceptors to call, excluding the 
interceptor if any of the actions in excludes is called.." }
  add-controller-interceptor [controller-interceptor controller excludes]
  (reset! controller-interceptors
    (assoc-controller-excludes-interceptors @controller-interceptors controller-interceptor controller excludes)))

(defn
#^{ :doc "Adds the given interceptor to the given controller, including or excluding the given actions. Note, adding
includes will completely ignore excludes." }
  add-interceptor [interceptor controller excludes includes]
  (when (and interceptor controller)
    (if (and includes (not-empty includes))
      (doseq [include-action includes]
        (add-action-interceptor interceptor controller include-action))
      (add-controller-interceptor interceptor controller excludes))))

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
  find-action-interceptor [controller action]
  (get (get @action-interceptors (keyword controller)) (keyword action)))

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
        (get @controller-interceptors (keyword controller))))))

(defn
#^{ :doc "Returns true if the app interceptor in the given interceptor map should be called for the given controller 
and action." }
  call-app-interceptor? [interceptor-map controller action]
  (let [action-set (get (:excludes interceptor-map) (keyword controller))]
    (if action-set
      (when (and (set? action-set) (not-empty action-set))
        (not (contains? action-set (keyword action))))
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
      (find-action-interceptor controller action)
      (concat 
        (find-controller-interceptors controller action)
        (find-app-interceptors controller action)))
    (fn [request-map action-fn]
      (action-fn request-map))))

(defn
#^{ :doc "Runs all interceptors passing the given request-map and action function. If there are no interceptors for the
given action and controller, then this function simply runs the action function passing request-map to it.." }
  run-interceptors [{ :keys [controller action] :as request-map } action-fn]
  ((create-interceptor-chain controller action) request-map action-fn))

(defn
#^{ :doc "Attempts to run the action requested in request-map. If the action is successful, it's response is returned, 
otherwise nil is returned." }
  run-action [request-map]
  (let [action-fn (find-action-fn request-map)]
    (when action-fn
      (logging/debug (str "Running action: " (fully-qualified-action request-map)))
      (run-interceptors request-map action-fn))))

(defn
#^{ :doc "Calls the given controller with the given request map returning the response." }
  call-controller [request-map]
  (if environment/reload-files
    (do 
      (load-controller (:controller request-map))
      (run-action request-map))
    (or 
      (run-action request-map)
      (do
        (load-controller (:controller request-map))
        (run-action request-map)))))