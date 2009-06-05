(ns conjure.migration.migration
  (:import [java.io File])
  (:use [conjure.server.jdbc-connector :as jdbc-connector]
        [conjure.util.string-utils :as string-utils]
        [conjure.util.loading-utils :as loading-utils]
        [clojure.contrib.str-utils :as clojure-str-utils]))

(def schema_info_table "schema_info")
(def version_column "version")

(defn 
#^{:doc "Finds the db directory which contains all of the files for updating the schema for the database."}
  find-db-directory []
  (loading-utils/get-classpath-dir-ending-with "db"))

(defn
#^{:doc "Finds the migrate directory."}
  find-migrate-directory []
  (let [migrate-directory (new File (. (find-db-directory) getPath) "migrate")]
    (if (. migrate-directory exists)
      migrate-directory
      nil)))

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
#^{:doc "Returns all of the migration files as a collection."}
  all-migration-files [migrate-directory]
  (filter 
    (fn [migrate-file] 
      (re-find #"^[0-9]+_.+\.clj" (. migrate-file getName))) 
    (. migrate-directory listFiles)))

(defn 
#^{:doc "Returns all of the migration file names as a collection."}
  all-migration-file-names [migrate-directory]
  (map 
    (fn [migration-file] (. migration-file getName))
    (all-migration-files migrate-directory)))
    
(defn
#^{:doc "Returns the migration number from the given migration file name."}
  migration-number-from-name [migration-file-name]
  (. Integer parseInt (re-find #"^[0-9]+" migration-file-name)))
  
(defn
#^{:doc "Returns the migration number from the given migration file."}
  migration-number-from-file [migration-file]
  (migration-number-from-name (. migration-file getName)))
    
(defn
#^{:doc "Returns all of the migration file names with numbers between low-number and high-number inclusive."}
  migration-files-in-range [low-number high-number]
  (let [migrate-directory (find-migrate-directory)]
    (filter 
      (fn [migration-file] 
        (let [migration-number (migration-number-from-file migration-file)]
          (and (>= migration-number low-number) (<= migration-number high-number)))) 
      (all-migration-files migrate-directory))))

(defn 
#^{:doc "Returns all of the numbers prepended to the migration files."}
  all-migration-numbers [migrate-directory]
  (map 
    (fn [migration-file-name] (migration-number-from-name migration-file-name)) 
    (all-migration-file-names migrate-directory)))

(defn
#^{:doc "Returns the maximum number of all migration files."}
  max-migration-number [migrate-directory]
  (let [migration-numbers (all-migration-numbers migrate-directory)]
    (if (> (count migration-numbers) 0) 
      (eval (cons max migration-numbers))
      0)))

(defn 
#^{:doc "Returns the next number to use for a migration file."}
  find-next-migrate-number [migrate-directory]
  (+ (max-migration-number migrate-directory) 1))

(defn
#^{:doc "Creates a new migration file from the given migration name."}
  create-migration-file [migrate-directory migration-name]
  (let [next-migrate-number (find-next-migrate-number migrate-directory)
        migration-file-name (str (string-utils/prefill (str next-migrate-number) 3 "0") "_" (loading-utils/dashes-to-underscores migration-name) ".clj")
        migration-file (new File migrate-directory  migration-file-name)]
    (println "Creating migration file" migration-file-name "...")
    (. migration-file createNewFile)
    migration-file))

(defn 
#^{:doc "Gets the current db version number. If the schema info table doesn't exist this function creates it. If the schema info table is empty, then it adds a row and sets the version to 0."}
  current-db-version []
  (let [schema-info-table "schema_info"]
    (if (jdbc-connector/table-exists? schema-info-table)
      (do
        (println schema-info-table "exists")
        (let [version-results (jdbc-connector/sql-find {:table schema-info-table})]
          (if (. version-results next)
            (. version-results getInt 1)
            (do
              (println schema-info-table "is empty.")
              (jdbc-connector/insert-one-into schema-info-table '("version") '(0))
              0))))
      (do
        (println schema-info-table "does not exist. Creating table...")
        (jdbc-connector/create-table schema-info-table {:version "INT NOT NULL"})
        (println "Setting the initial version to 0.")
        (jdbc-connector/insert-one-into schema-info-table '("version") '(0))
        0))))
        
(defn
#^{:doc "Returns the migration namespace for the given migration file."}
  migration-namespace [migration-file]
  (loading-utils/namespace-string-for-file "migrate" (. migration-file getName)))
  
(defn
#^{:doc "Runs the up function in the given migration file."}
  run-migrate-up [migration-file]
  (println "Running" (. migration-file getName) "up...")
  (load-file (. migration-file getAbsolutePath))
  (load-string (str "(" (migration-namespace migration-file) "/up)")))
  
(defn
#^{:doc "Runs the down function in the given migration file."}
  run-migrate-down [migration-file]
  (println "Running" (. migration-file getName) "down...")
  (load-file (. migration-file getAbsolutePath))
  (load-string (str "(" (migration-namespace migration-file) "/down)")))
        
(defn
#^{:doc "Runs the up function on all of the given migration files."}
  migrate-up-all [migration-files]
  (if (seq migration-files)
    (let [migration-file (first migration-files)]
      (run-migrate-up migration-file)
      (let [output (migrate-up-all (rest migration-files))]
        (if output 
          output 
          (migration-number-from-file migration-file))))))

(defn
#^{:doc "Finds the number of the migration file before the given number"}
  migration-number-before 
    ([migration-number] (migration-number-before migration-number (all-migration-files (find-migrate-directory))))
    ([migration-number migration-files]
      (let [migration-file (first migration-files)
            migration-file-number (migration-number-from-file migration-file)]
        (if (< migration-file-number migration-number)
          (let [next-migration-number (migration-number-before migration-number (rest migration-files))]
            (if (not (== next-migration-number 0))
              next-migration-number
              migration-number))
          0))))

(defn
#^{:doc "Runs the up function on all of the given migration files."}
  migrate-down-all [migration-files]
  (if (seq migration-files)
    (let [migration-file (first migration-files)]
      (run-migrate-down migration-file)
      (let [output (migrate-down-all (rest migration-files))]
        (if output 
          output 
          (migration-number-before (migration-number-from-file migration-file)))))))

(defn
#^{:doc "Updates the version number saved in the schema table in the database."}
  update-db-version [new-version]
  (jdbc-connector/update  schema_info_table {:set (str version_column " = " new-version)}))

(defn
#^{:doc "Migrates the database up from from-version to to-version."}
  migrate-up [from-version to-version]
  (let [new-version (migrate-up-all (migration-files-in-range from-version to-version))]
    (if new-version
      (do
        (println "Migrated to version:" new-version)
        (update-db-version new-version))
      (println "No changes were made to the database."))))
  
(defn
#^{:doc "Migrates the database down from from-version to to-version."}
  migrate-down [from-version to-version]
  (let [new-version (migrate-down-all (reverse (migration-files-in-range to-version from-version)))]
    (if new-version
      (do
        (println "Migrated to version:" new-version)
        (update-db-version new-version))
      (println "No changes were made to the database."))))

(defn 
#^{:doc "Updates the database to the given version number. If the version number is less than the current database version number, then this function causes a roll back."}
  update-to-version [version-number]
  (let [db-version (current-db-version)]
    (println "Current database version: " (str db-version))
    (let [version-number-min (min (max version-number 0) (max-migration-number (find-migrate-directory)))]
      (println "Updating to version:" version-number-min)
      (if (< db-version version-number-min)
        (migrate-up (+ db-version 1) version-number-min)
        (migrate-down db-version (+ version-number-min 1))))))