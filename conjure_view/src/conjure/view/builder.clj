(ns conjure.view.builder
  (:import [java.io File])
  (:require [clojure.tools.logging :as logging]
            [conjure.view.util :as util]
            [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.string-utils :as conjure-str-utils]))

(defn 
#^{:doc "Finds or creates if missing, a service directory for the given service in the given views directory."}
  find-or-create-service-directory
  ([{ :keys [views-directory service silent] :or {views-directory (util/find-views-directory), silent false } }]
    (when (and views-directory service)
      (let [service-directory (util/find-service-directory views-directory service)]
        (if service-directory
          (do
            (logging/info (str (.getPath service-directory) " directory already exists."))
            service-directory)
          (do
            (logging/info "Creating service directory in views...")
            (let [new-service-directory (new File views-directory (loading-utils/dashes-to-underscores service))]
              (.mkdirs new-service-directory)
              new-service-directory)))))))

(defn
#^{:doc "Creates a new view file from the given migration name."}
  create-view-file 
  ([{ :keys [service-directory action silent] :or { silent false } }] 
    (when (and service-directory action)
      (let [view-file-name (str (loading-utils/dashes-to-underscores (conjure-str-utils/str-keyword action)) ".clj")
            view-file (new File service-directory  view-file-name)]
        (if (.exists view-file)
          (do
            (logging/info (str (.getName view-file) " already exists. Doing nothing."))
            view-file)
          (do
            (logging/info (str "Creating view file " view-file-name "..."))
            (.createNewFile view-file)
            view-file))))))