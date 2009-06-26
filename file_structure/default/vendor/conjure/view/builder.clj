(ns conjure.view.builder
  (:import [java.io File])
  (:require [conjure.view.util :as util]
            [conjure.util.loading-utils :as loading-utils]))

(defn 
#^{:doc "Finds or creates if missing, a controller directory for the given controller in the given view directory."}
  find-or-create-controller-directory [view-directory controller]
  (let [controller-directory (util/find-controller-directory view-directory controller)]
    (if controller-directory
      (do
        (println (. controller-directory getPath) "directory already exists.")
        controller-directory)
      (do
        (println "Creating controller directory in views...")
        (let [new-controller-directory (new File view-directory (loading-utils/dashes-to-underscores controller))]
          (. new-controller-directory mkdirs)
          new-controller-directory)))))

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