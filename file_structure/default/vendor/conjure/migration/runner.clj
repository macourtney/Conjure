(ns conjure.migration.runner
  (:import [java.io File])
  (:require [conjure.migration.util :as util]
            [conjure.model.database :as database]))

(defn
#^{:doc "Prints the given args to standard out if quiet evaluates to true. Otherwise, do nothing."}
  quiet-println [quiet & args]
  (if (not quiet) (apply println args)))

(defn 
#^{:doc "Gets the current db version number. If the schema info table doesn't exist this function creates it. If the 
schema info table is empty, then it adds a row and sets the version to 0."}
  current-db-version
  ([] (current-db-version false))
  ([quiet]
    (if (database/table-exists? util/schema-info-table)
      (do
        (quiet-println quiet util/schema-info-table "exists")
        (let [version-results (database/sql-find { :table util/schema-info-table :limit 1 })
              version-result-map (first version-results)]
          (if version-result-map
            (get version-result-map util/version-column)
            (do
              (if (not quiet) (println util/schema-info-table "is empty."))
              (database/insert-into util/schema-info-table { util/version-column 0 })
              0))))
      (do
        (quiet-println quiet  util/schema-info-table "does not exist. Creating table...")
        (database/create-table util/schema-info-table 
          (database/integer "version" { :not-null true }))
        (quiet-println quiet  "Setting the initial version to 0.")
        (database/insert-into util/schema-info-table {util/version-column 0})
        0))))

(defn
#^{:doc "Updates the version number saved in the schema table in the database."}
  update-db-version [new-version]
  (database/update util/schema-info-table ["true"] { util/version-column new-version }))

(defn
#^{:doc "Runs the up function in the given migration file."}
  run-migrate-up 
  ([migration-file] (run-migrate-up migration-file false))
  ([migration-file quiet]
    (if (and migration-file (instance? File migration-file))
      (do
        (quiet-println quiet "Running" (. migration-file getName) "up...")
        (load-file (. migration-file getAbsolutePath))
        (load-string (str "(" (util/migration-namespace migration-file) "/up)"))
        (let [new-version (util/migration-number-from-file migration-file)]
          (update-db-version new-version)
          new-version))
      (quiet-println quiet "Invalid migration-file:" migration-file "No changes were made to the database."))))
  
(defn
#^{:doc "Runs the down function in the given migration file."}
  run-migrate-down 
  ([migration-file] (run-migrate-down migration-file false))
  ([migration-file quiet]
    (if (and migration-file (instance? File migration-file))
      (do
        (quiet-println quiet "Running" (. migration-file getName) "down...")
        (load-file (. migration-file getAbsolutePath))
        (load-string (str "(" (util/migration-namespace migration-file) "/down)"))
        (let [new-version (util/migration-number-before (util/migration-number-from-file migration-file))]
          (update-db-version new-version)
          new-version))
      (quiet-println quiet "Invalid migration-file:" migration-file "No changes were made to the database."))))
        
(defn
#^{:doc "Runs the up function on all of the given migration files."}
  migrate-up-all
  ([] (migrate-up-all (util/all-migration-files) false))
  ([migration-files] (migrate-up-all migration-files false))
  ([migration-files quiet]
    (loop [other-migrations migration-files
           output nil]
      (if (not-empty other-migrations)
        (recur
          (rest other-migrations)
          (run-migrate-up (first other-migrations) quiet))
        output))))

(defn
#^{:doc "Runs the up function on all of the given migration files."}
  migrate-down-all
  ([] (migrate-down-all (reverse (util/all-migration-files)) false))
  ([migration-files] (migrate-down-all migration-files false))
  ([migration-files quiet]
    (loop [other-migrations migration-files
           output nil]
      (if (not-empty other-migrations)
        (recur
          (rest other-migrations)
          (run-migrate-down (first other-migrations) quiet))
        output))))

(defn
#^{:doc "Migrates the database up from from-version to to-version."}
  migrate-up
  ([from-version to-version] (migrate-up from-version to-version false))
  ([from-version to-version quiet]
    (if (and from-version to-version)
      (let [new-version (migrate-up-all (util/migration-files-in-range from-version to-version) quiet)]
        (if new-version
          (quiet-println quiet "Migrated to version:" new-version)
          (quiet-println quiet "No changes were made to the database.")))
      (quiet-println quiet "Invalid version number:" from-version "or" to-version "No changes were made to the database."))))
  
(defn
#^{:doc "Migrates the database down from from-version to to-version."}
  migrate-down
  ([from-version to-version] (migrate-down from-version to-version false))
  ([from-version to-version quiet]
    (if (and from-version to-version)
      (let [new-version (migrate-down-all (reverse (util/migration-files-in-range to-version from-version)) quiet)]
        (if new-version
          (quiet-println quiet "Migrated to version:" new-version)
          (quiet-println quiet "No changes were made to the database.")))
      (quiet-println quiet "Invalid version number:" from-version "or" to-version "No changes were made to the database."))))

(defn 
#^{:doc "Updates the database to the given version number. If the version number is less than the current database 
version number, then this function causes a roll back."}
  update-to-version
  ([version-number] (update-to-version version-number false))
  ([version-number quiet]
    (if version-number
      (let [db-version (current-db-version quiet)]
        (quiet-println quiet "Current database version:" (str db-version))
        (let [version-number-min (min (max version-number 0) (util/max-migration-number (util/find-migrate-directory)))]
          (quiet-println quiet "Updating to version:" version-number-min)
          (if (< db-version version-number-min)
            (migrate-up (+ db-version 1) version-number-min quiet)
            (migrate-down db-version (+ version-number-min 1) quiet))))
      (quiet-println quiet "Invalid version-number:" version-number))))