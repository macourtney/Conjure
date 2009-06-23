(ns conjure.view.view
  (:import [java.io File])
  (:use [conjure.util.loading-utils :as loading-utils]
        [clojure.contrib.seq-utils :as seq-utils]
        [conjure.util.string-utils :as string-utils]
        [conjure.util.file-utils :as file-utils]))

(defn 
#^{:doc "Finds the views directory which contains all of the files which describe the html pages of the app."}
  find-views-directory []
  (seq-utils/find-first (fn [directory] (. (. directory getPath) endsWith "views"))
    (. (loading-utils/get-classpath-dir-ending-with "app") listFiles)))
  
(defn
#^{:doc "Finds a controller directory for the given controller in the given view directory."}
  find-controller-directory [view-directory controller]
  (file-utils/find-directory view-directory (loading-utils/dashes-to-underscores controller)))
    
(defn 
#^{:doc "Finds or creates if missing, a controller directory for the given controller in the given view directory."}
  find-or-create-controller-directory [view-directory controller]
  (let [controller-directory (find-controller-directory view-directory controller)]
    (if controller-directory
      (do
        (println (. controller-directory getPath) "directory already exists.")
        controller-directory)
      (do
        (println "Creating controller directory in views...")
        (let [new-controller-directory (new File view-directory (dashes-to-underscores controller))]
          (. new-controller-directory mkdirs)
          new-controller-directory)))))
        
(defn
#^{:doc "Finds a view file with the given controller-directory and action."}
  find-view-file [controller-directory action]
  (file-utils/find-file controller-directory (symbol-string-to-clj-file action)))
      
(defn
#^{:doc "Creates a new view file from the given migration name."}
  create-view-file [controller-directory action]
  (let [view-file-name (str (loading-utils/dashes-to-underscores action) ".clj")
        view-file (new File controller-directory  view-file-name)]
    (if (. view-file exists)
      (println (. view-file getName) "already exits. Doing nothing.")
      (do
        (println "Creating view file" view-file-name "...")
        (. view-file createNewFile)
        view-file))))
  
(defn
#^{:doc "Loads the view corresponding to the values in the given request map."}
  load-view [request-map]
  (loading-utils/load-resource 
    (str "views/" (loading-utils/dashes-to-underscores (:controller request-map))) 
    (str (loading-utils/dashes-to-underscores (:action request-map)) ".clj")))

(defn
#^{:doc "Returns the view namespace request map."}
  request-view-namespace [request-map]
  (str "views." (:controller request-map) "." (:action request-map)))
  
(defn
#^{:doc "Returns the view namespace for the given view file."}
  view-namespace [controller view-file]
  (request-view-namespace 
    { :controller controller 
      :action (loading-utils/clj-file-to-symbol-string (. view-file getName)) }))
 
(defmacro
#^{:doc "Defines a view. This macro should be used in a view file to define the parameters used in the view."}
  defview [params & body]
  (let [render-view "render-view"
        request-map "request-map"]
    `(defn ~'render-view [~'request-map ~@params]
      ~@body)))