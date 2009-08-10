(ns conjure.view.util
  (:require [clojure.contrib.seq-utils :as seq-utils]
            [conjure.util.file-utils :as file-utils]
            [conjure.util.loading-utils :as loading-utils]))

(defn 
#^{:doc "Finds the views directory which contains all of the files which describe the html pages of the app."}
  find-views-directory []
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith "views"))
    (. (loading-utils/get-classpath-dir-ending-with "app") listFiles)))
  
(defn
#^{:doc "Finds a controller directory for the given controller in the given view directory."}
  find-controller-directory 
  ([controller] (find-controller-directory (find-views-directory) controller))
  ([view-directory controller]
    (if controller
      (file-utils/find-directory view-directory (loading-utils/dashes-to-underscores controller)))))
  
(defn
#^{:doc "Finds a view file with the given controller-directory and action."}
  find-view-file [controller-directory action]
  (if (and controller-directory action)
    (file-utils/find-file controller-directory (loading-utils/symbol-string-to-clj-file action))))
  
(defn
#^{:doc "Loads the view corresponding to the values in the given request map."}
  load-view [request-map]
  (loading-utils/load-resource 
    (str "views/" (loading-utils/dashes-to-underscores (:controller request-map))) 
    (str (loading-utils/dashes-to-underscores (:action request-map)) ".clj")))

(defn
#^{:doc "Returns the view namespace request map."}
  request-view-namespace [request-map]
  (if request-map
    (str "views." (loading-utils/underscores-to-dashes (:controller request-map)) "." 
      (loading-utils/underscores-to-dashes (:action request-map)))))

(defn
#^{:doc "Returns the view namespace for the given controller and action."}
  view-namespace-by-action [controller action]
  (if (and controller action)
    (request-view-namespace 
      { :controller controller 
        :action action })))

(defn
#^{:doc "Returns the view namespace for the given view file."}
  view-namespace [controller view-file]
  (if (and controller view-file)
    (view-namespace-by-action controller (loading-utils/clj-file-to-symbol-string (. view-file getName)))))

(defn
#^{:doc "Returns the rendered view from the given request-map."}
  render-view [request-map & params]
  (load-view request-map)
  (apply
    (eval (read-string (str (request-view-namespace request-map) "/render-view")))
    request-map params))

(defn
#^{:doc "Returns the rendered layout for the given layout name."}
  render-layout [layout-name request-map body]
  (let [layouts-request-map (assoc request-map :controller "layouts")
        application-request-map (assoc layouts-request-map :action (if layout-name layout-name "application"))]
    (render-view application-request-map body)))