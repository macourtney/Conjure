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
      #(. (. % getName) endsWith controller-file-name-ending) 
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
#^{ :doc "Attempts to run the action requested in request-map. If the action is successful, it's response is returned, 
otherwise nil is returned." }
  run-action [request-map]
  (let [action-fn (find-action-fn request-map)]
    (when action-fn
      (logging/debug (str "Running action: " (fully-qualified-action request-map)))
      (action-fn request-map))))

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
