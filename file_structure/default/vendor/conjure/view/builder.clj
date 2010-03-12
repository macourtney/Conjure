(ns conjure.view.builder
  (:import [java.io File])
  (:require [clojure.contrib.logging :as logging]
            [conjure.view.util :as util]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.string-utils :as conjure-str-utils]))

(defn 
#^{:doc "Finds or creates if missing, a controller directory for the given controller in the given views directory."}
  find-or-create-controller-directory
  ([{ :keys [views-directory controller silent] :or {views-directory (util/find-views-directory), silent false } }]
    (if (and views-directory controller)
      (let [controller-directory (util/find-controller-directory views-directory controller)]
        (if controller-directory
          (do
            (logging/info (str (. controller-directory getPath) " directory already exists."))
            controller-directory)
          (do
            (logging/info "Creating controller directory in views...")
            (let [new-controller-directory (new File views-directory (loading-utils/dashes-to-underscores controller))]
              (. new-controller-directory mkdirs)
              new-controller-directory)))))))

(defn
#^{:doc "Creates a new view file from the given migration name."}
  create-view-file 
  ([{ :keys [controller-directory action silent] :or { silent false } }] 
    (if (and controller-directory action)
      (let [view-file-name (str (loading-utils/dashes-to-underscores (conjure-str-utils/str-keyword action)) ".clj")
            view-file (new File controller-directory  view-file-name)]
        (if (. view-file exists)
          (do
            (logging/info (str (. view-file getName) " already exists. Doing nothing."))
            view-file)
          (do
            (logging/info (str "Creating view file " view-file-name "..."))
            (. view-file createNewFile)
            view-file))))))