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
  (file-utils/find-directory view-directory controller))
    
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
        (. controller-directory mkdirs)
        controller-directory))))
        
(defn
#^{:doc "Finds a view file with the given controller-directory and action."}
  find-view-file [controller-directory action]
  (file-utils/find-file controller-directory (str action ".clj")))
      
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
#^{:doc "Returns the view namespace for the given view file."}
  view-namespace [controller view-file]
  (str "views." controller "." (loading-utils/clj-file-to-symbol-string (. view-file getName))))