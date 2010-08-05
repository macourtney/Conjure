(ns conjure.core.migration.util
  (:require [clojure.contrib.command-line :as command-line]
            [clojure.contrib.logging :as logging]
            [conjure.core.model.database :as database]
            [conjure.core.server.server :as server]
            [conjure.core.util.string-utils :as string-utils]))

(def schema-info-table "schema_info")
(def version-column :version)

(defn init [args]
  (command-line/with-command-line args
    "lein migrate [options]"
    [ [version "The version to migrate to. Example: -version 2 -> migrates to version 2." nil]
      [mode "The server mode. For example, development, production, or test." nil]
      remaining]
  
    (server/set-mode mode)
    (server/init)))

(defn
  version-table-is-empty []
  (logging/info (str schema-info-table " is empty. Setting the initial version to 0."))
  (database/insert-into schema-info-table { version-column 0 })
  0)

(defn
  version-table-exists []
  (logging/info (str schema-info-table " exists"))
  (if-let [version-result-map (first (database/sql-find { :table schema-info-table :limit 1 }))]
    (get version-result-map version-column)
    (version-table-is-empty)))

(defn
  version-table-does-not-exist []
  (logging/info (str schema-info-table " does not exist. Creating table..."))
  (database/create-table schema-info-table 
    (database/integer (string-utils/str-keyword version-column) { :not-null true }))
  (version-table-is-empty))

(defn 
#^{:doc "Gets the current db version number. If the schema info table doesn't exist this function creates it. If the 
schema info table is empty, then it adds a row and sets the version to 0."}
  current-version []
  (if (database/table-exists? schema-info-table)
    (version-table-exists)
    (version-table-does-not-exist)))

(defn
#^{:doc "Updates the version number saved in the schema table in the database."}
  update-version [new-version]
  (database/update schema-info-table ["true"] { version-column new-version }))