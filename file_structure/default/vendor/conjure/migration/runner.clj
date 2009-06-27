(ns conjure.migration.runner
  (:require [conjure.migration.util :as util]
            [conjure.model.database :as database]))

(defn 
#^{:doc "Gets the current db version number. If the schema info table doesn't exist this function creates it. If the 
schema info table is empty, then it adds a row and sets the version to 0."}
  current-db-version []
    (if (database/table-exists? util/schema-info-table)
      (do
        (println util/schema-info-table "exists")
        (let [version-results (database/sql-find { :table util/schema-info-table :limit 1 })
              version-result-map (first version-results)]
          (if version-result-map
            (get version-result-map util/version-column)
            (do
              (println util/schema-info-table "is empty.")
              (database/insert-into util/schema-info-table { util/version-column 0 })
              0))))
      (do
        (println util/schema-info-table "does not exist. Creating table...")
        (database/create-table util/schema-info-table 
          (database/integer "version" { :not-null true }))
        (println "Setting the initial version to 0.")
        (database/insert-into util/schema-info-table {util/version-column 0})
        0)))

(defn
#^{:doc "Updates the version number saved in the schema table in the database."}
  update-db-version [new-version]
  (database/update util/schema-info-table ["true"] { util/version-column new-version }))

(defn
#^{:doc "Runs the up function in the given migration file."}
  run-migrate-up [migration-file]
  (println "Running" (. migration-file getName) "up...")
  (load-file (. migration-file getAbsolutePath))
  (load-string (str "(" (util/migration-namespace migration-file) "/up)"))
  (let [new-version (util/migration-number-from-file migration-file)]
    (update-db-version new-version)
    new-version))
  
(defn
#^{:doc "Runs the down function in the given migration file."}
  run-migrate-down [migration-file]
  (println "Running" (. migration-file getName) "down...")
  (load-file (. migration-file getAbsolutePath))
  (load-string (str "(" (util/migration-namespace migration-file) "/down)"))
  (let [new-version (util/migration-number-before (util/migration-number-from-file migration-file))]
    (update-db-version new-version)
    new-version))
        
(defn
#^{:doc "Runs the up function on all of the given migration files."}
  migrate-up-all [migration-files]
  (loop [other-migrations migration-files
         output nil]
    (if (not-empty other-migrations)
      (recur
        (rest other-migrations)
        (run-migrate-up (first other-migrations)))
      output)))

(defn
#^{:doc "Runs the up function on all of the given migration files."}
  migrate-down-all [migration-files]
  (loop [other-migrations migration-files
         output nil]
    (if (not-empty other-migrations)
      (recur
        (rest other-migrations)
        (run-migrate-down (first other-migrations)))
      output)))

(defn
#^{:doc "Migrates the database up from from-version to to-version."}
  migrate-up [from-version to-version]
  (let [new-version (migrate-up-all (util/migration-files-in-range from-version to-version))]
    (if new-version
      (println "Migrated to version:" new-version)
      (println "No changes were made to the database."))))
  
(defn
#^{:doc "Migrates the database down from from-version to to-version."}
  migrate-down [from-version to-version]
  (let [new-version (migrate-down-all (reverse (util/migration-files-in-range to-version from-version)))]
    (if new-version
      (println "Migrated to version:" new-version)
      (println "No changes were made to the database."))))

(defn 
#^{:doc "Updates the database to the given version number. If the version number is less than the current database 
version number, then this function causes a roll back."}
  update-to-version [version-number]
  (let [db-version (current-db-version)]
    (println "Current database version: " (str db-version))
    (let [version-number-min (min (max version-number 0) (util/max-migration-number (util/find-migrate-directory)))]
      (println "Updating to version:" version-number-min)
      (if (< db-version version-number-min)
        (migrate-up (+ db-version 1) version-number-min)
        (migrate-down db-version (+ version-number-min 1))))))