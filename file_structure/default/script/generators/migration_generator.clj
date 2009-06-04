(ns generators.migration-generator
  (:import [java.io File FileWriter])
  (:use [conjure.migration.migration :as migration]))

(defn
#^{:doc "Prints out how to use the generate migration command."}
  migration-usage []
  (println "You must supply a migration name (Like migration-name).")
  (println "Usage: ./run.sh script/generate.clj migration <migration name>"))
  
(defn
#^{:doc "Generates the migration content and saves it into the given migration file."}
  generate-file-content [migration-file migration-name]
  (let [migration-number (migration-number-from-file migration-file)
        migration-namespace (migration/migration-namespace migration-file)
        content (str "(ns " migration-namespace "
  (:use [conjure.server.jdbc-connector :as jdbc-connector]))

(defn
#^{:doc \"Migrates the database up to version " migration-number ".\"}
  up []
  (println \"" migration-namespace " up...\"))
  
(defn
#^{:doc \"Migrates the database down from version " migration-number ".\"}
  down []
  (println \"" migration-namespace " down...\"))")
        migration-file-writer (new FileWriter migration-file)]
    (. migration-file-writer write content 0 (. content length))
    (. migration-file-writer flush)
    (. migration-file-writer close)))

(defn
#^{:doc "Creates the migration file from the given migration-name."}
  generate-migration-file [migration-name]
  (if migration-name
      (let [db-directory (find-db-directory)]
        (if db-directory
          (let [migrate-directory (migration/find-or-create-migrate-directory db-directory)
                migration-file (migration/create-migration-file migrate-directory migration-name)] 
            (generate-file-content migration-file migration-name))
          (println "Could not find db directory.")))
      (migration-usage)))

(defn 
#^{:doc "Generates a migration file for the migration name given in params."}
  generate-migration [params]
  (generate-migration-file (first params)))