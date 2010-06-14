(ns destroyers.migration-destroyer
  (:require [clojure.contrib.logging :as logging]
            [conjure.migration.util :as util]))

(defn
#^{:doc "Prints out how to use the destroy migration command."}
  migration-usage []
  (println "You must supply a migration name (Like migration-name).")
  (println "Usage: ./run.sh script/destroy.clj migration <migration name>"))
  
(defn
#^{:doc "Creates the migration file from the given migration-name."}
  destroy-migration-file [migration-name]
  (if migration-name
    (let [migrate-directory (util/find-migrate-directory)]
      (if migrate-directory
        (let [migration-file (util/find-migration-file migrate-directory migration-name)]
           (if migration-file
             (let [is-deleted (. migration-file delete)]
               (logging/info (str "File " (. migration-file getPath) (if is-deleted " deleted." " not deleted.") )))
             (logging/error (str "Could not find migration file for " migration-name))))
        (logging/error "Could not find db directory.")))
    (migration-usage)))

(defn
#^{:doc "Destroys a migration file for the migration name given in params."}
  destroy [params]
  (destroy-migration-file (first params)))

(defn
#^{:doc "Destroys all of the files created by the migration_generator."}
  destroy-all-dependencies
  ([migration-name]
    (destroy-migration-file migration-name)))