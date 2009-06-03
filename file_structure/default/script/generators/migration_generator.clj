(ns generators.migration-generator
  (:import [java.io File])
  (:use [conjure.migration.migration :as migration]))

(defn
#^{:doc "Prints out how to use the generate migration command."}
  migration-usage []
  (println "You must supply a migration name (Like migration-name).")
  (println "Usage: ./run.sh script/generate.clj migration <migration name>"))

(defn 
#^{:doc "Generates a migration file for the migration name given in params."}
  generate-migration [params]
  (let [migration-name (first params)]
    (if migration-name
      (let [db-directory (find-db-directory)]
        (if db-directory
          (let [migrate-directory (migration/find-or-create-migrate-directory db-directory)] 
            (migration/create-migration-file migrate-directory (first params)))
          (println "Could not find db directory.")))
      (migration-usage))))