(ns destroyers.migration-destroyer
  (:require [conjure.migration.migration :as migration]))

(defn
#^{:doc "Prints out how to use the destroy migration command."}
  migration-usage []
  (println "You must supply a migration name (Like migration-name).")
  (println "Usage: ./run.sh script/destroy.clj migration <migration name>"))
  
(defn
#^{:doc "Creates the migration file from the given migration-name."}
  destroy-migration-file [migration-name]
  (if migration-name
    (let [migrate-directory (migration/find-migrate-directory)]
      (if migrate-directory
        (let [migration-file (migration/find-migration-file migrate-directory migration-name)]
           (if migration-file
             (do
               (. migration-file delete)
               (println "File" (. migration-file getPath) "deleted."))
             (println "Could not find migration file for" migration-name)))
        (println "Could not find db directory.")))
    (migration-usage)))

(defn
#^{:doc "Destroys a migration file for the migration name given in params."}
  destroy-migration [params]
  (destroy-migration-file (first params)))