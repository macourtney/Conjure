(ns conjure.binding.util
  (:import [java.io File])
  (:require [clojure.contrib.logging :as logging]
            [clojure.contrib.str-utils :as str-utils]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.loading-utils :as loading-utils]
            environment))

(def bindings-dir "bindings")
(def bindings-namespace bindings-dir)

(def bindings (atom {}))

(defn 
#^{ :doc "Finds the bindings directory." }
  find-bindings-directory [] 
  (new File (loading-utils/get-classpath-dir-ending-with "app") bindings-dir))
  
(defn 
#^{ :doc "Finds the controller directory." }
  find-controllers-directory 
  ([controller] (find-controllers-directory (find-bindings-directory) controller))
  ([bindings-directory controller]
    (new File bindings-directory (loading-utils/dashes-to-underscores controller))))

(defn
#^{ :doc "Returns the controller file name for the given controller name." }
  binding-file-name-string [action]
  (when (and action (not-empty action))
    (str (loading-utils/dashes-to-underscores action) ".clj")))

(defn
#^{ :doc "Finds a binding file with the given controller name." }
  find-binding-file
  ([controller-name action-name] (find-binding-file (find-bindings-directory) controller-name action-name)) 
  ([controllers-directory controller-name action-name]
    (if (and controller-name action-name)
      (file-utils/find-file 
        (new File controllers-directory (loading-utils/dashes-to-underscores controller-name)) 
        (binding-file-name-string action-name)))))

(defn
#^{ :doc "Returns the path to the binding from the bindings directory." }
  bindings-path-to-binding [controller action]
  (str (loading-utils/dashes-to-underscores controller) "/" (binding-file-name-string action)))

(defn
#^{ :doc "Returns the path to the binding from the app directory." }
  app-path-to-binding [controller action]
  (str bindings-dir "/" (bindings-path-to-binding controller action)))

(defn
#^{ :doc "Returns true if the given controller exists." }
  binding-exists? [controller action]
  (if (and controller action)
    (loading-utils/resource-exists? 
      (app-path-to-binding controller action))))

(defn
#^{ :doc "Returns the controller namespace for the given controller." }
  binding-namespace [controller action]
  (if (and controller action)
    (str bindings-namespace "." (loading-utils/underscores-to-dashes controller) "." 
      (loading-utils/underscores-to-dashes action))))

(defn
#^{ :doc "Returns the controller and action from the given binding namespace as a map." }
  controller-action-map [namespace-name]
  (let [reverse-namespace (reverse (str-utils/re-split #"\." namespace-name))]
    { :controller (second reverse-namespace), :action (first reverse-namespace) }))

(defn
#^{ :doc "Returns the actions map for the given controller." }
  actions-map [controller]
  (when controller
    (get @bindings (keyword controller))))

(defn 
#^{ :doc "adds the given bind function into the given actions map and returns the result." }
  assoc-action [actions-map { :keys [bind-function action] }]
  (assoc actions-map (keyword action) bind-function))

(defn
#^{ :doc "adds the given bind function (:bind-function in params) into the given controllers map and returns the 
result." }
  assoc-controller [controllers-map { controller :controller, :as params }]
  (let [controller-key (keyword controller)]
    (assoc controllers-map controller-key 
      (assoc-action (get controllers-map controller-key) params))))

(defn
#^{ :doc "Adds the given bind function to the list of bind functions to call." }
  add-bind-function [bind-function params]
  (reset! bindings
    (assoc-controller @bindings 
      (assoc params :bind-function bind-function))))

(defn
#^{ :doc "Loads the given controller file." }
  load-binding [controller action]
  (when (binding-exists? controller action)
    (let [binding-namespace-str (binding-namespace controller action)]
      (require :reload (symbol binding-namespace-str))
      (loading-utils/reload-conjure-namespaces binding-namespace-str))))

(defn
#^{ :doc "Returns the binding function for the given controller and action." }
  find-binding-fn [controller action]
  (let [all-actions (actions-map controller)]
    (when all-actions
      (get all-actions (keyword action)))))

(defn
#^{ :doc "Returns fully qualified binding generated from the given request map." }
  fully-qualified-binding [controller action]
  (if (and controller action)
    (str (binding-namespace controller action))))

(defn
#^{ :doc "Attempts to run the binding requested in request-map. If the binding is successful, it's response is returned, 
otherwise nil is returned." }
  run-binding [controller action params]
  (let [binding-fn (find-binding-fn controller action)]
    (when binding-fn
      (logging/debug (str "Running binding: " (fully-qualified-binding controller action)))
      (apply binding-fn params))))

(defn
#^{ :doc "Calls the given binding with the given request map returning the response." }
  call-binding [controller action params]
  (if environment/reload-files
    (do 
      (load-binding controller action)
      (run-binding controller action params))
    (or 
      (run-binding controller action params)
      (do
        (load-binding controller action)
        (run-binding controller action params)))))