(ns conjure.migration.builder
  (:import [java.io File])
  (:require [conjure.migration.util :as util]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.string-utils :as string-utils]))

(defn 
#^{:doc "Finds or creates if missing, the migrate directory in the given db directory."}
  find-or-create-migrate-directory [db-directory]
  (let [migrate-directory (new File (. db-directory getPath) "migrate")]
    (if (. migrate-directory exists)
      (do
        (println "Migrate directory already exists.")
        migrate-directory)
      (do
        (println "Creating migrate directory...")
        (. migrate-directory mkdirs)
        migrate-directory))))

(defn
#^{:doc "Creates a new migration file from the given migration name."}
  create-migration-file [migrate-directory migration-name]
  (let [next-migrate-number (util/find-next-migrate-number migrate-directory)
        migration-file-name (str (string-utils/prefill (str next-migrate-number) 3 "0") "_" (loading-utils/dashes-to-underscores migration-name) ".clj")
        migration-file (new File migrate-directory  migration-file-name)]
    (println "Creating migration file" migration-file-name "...")
    (. migration-file createNewFile)
    migration-file))