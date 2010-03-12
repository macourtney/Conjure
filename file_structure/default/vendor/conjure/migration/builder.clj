(ns conjure.migration.builder
  (:import [java.io File])
  (:require [clojure.contrib.logging :as logging]
            [conjure.migration.util :as util]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.string-utils :as string-utils]))

(defn 
#^{:doc "Finds or creates if missing, the migrate directory in the given db directory."}
  find-or-create-migrate-directory
  ([] (find-or-create-migrate-directory (util/find-db-directory))) 
  ([db-directory]
    (if db-directory
      (let [migrate-directory (new File (. db-directory getPath) "migrate")]
        (if (. migrate-directory exists)
          (do
            (logging/info "Migrate directory already exists.")
            migrate-directory)
          (do
            (logging/info "Creating migrate directory...")
            (. migrate-directory mkdirs)
            migrate-directory))))))

(defn
#^{:doc "Creates a new migration file from the given migration name."}
  create-migration-file
  ([migration-name] (create-migration-file (find-or-create-migrate-directory) migration-name)) 
  ([migrate-directory migration-name]
    (if (and migrate-directory migration-name)
      (let [next-migrate-number (util/find-next-migrate-number migrate-directory)
            migration-file-name (str (string-utils/prefill (str next-migrate-number) 3 "0") "_" (loading-utils/dashes-to-underscores migration-name) ".clj")
            migration-file (new File migrate-directory  migration-file-name)]
        (logging/info (str "Creating migration file " migration-file-name "..."))
        (. migration-file createNewFile)
        migration-file))))