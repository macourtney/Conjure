(ns conjure.core.migration.util
  (:require [clojure.contrib.command-line :as command-line]
            [clojure.contrib.logging :as logging]
            [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.servlet-utils :as servlet-utils]
            [clojure.tools.string-utils :as string-utils]
            [conjure.core.model.database :as database]
            [conjure.core.server.request :as request]
            [conjure.core.server.server :as server]))

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

(defn
  file-separator-index [path-str]
  (when path-str
    (let [min-args (filter #(> % -1) [(.indexOf path-str "/" 1) (.indexOf path-str "\\" 1)])]
      (when (not-empty min-args)
        (apply min min-args)))))

(defn
  migrate-namespace-dir [migrate-dir-name]
  (if-let [file-separator-index (file-separator-index migrate-dir-name)]
    (.substring migrate-dir-name (inc file-separator-index))
     migrate-dir-name))

(defn
  all-migration-file-names [migrations-dir]
  (when-let [migrate-namespace-dir (migrate-namespace-dir migrations-dir)]
    (concat
      (loading-utils/all-class-path-file-names migrate-namespace-dir)
      (servlet-utils/all-file-names migrate-namespace-dir (request/servlet-context)))))

(defn
#^{ :doc "Returns the migration name for the given migration file." }
  migration-from-file [migration-file]
  (when migration-file
    (if (string? migration-file)
      (loading-utils/clj-file-to-symbol-string migration-file)
      (migration-from-file (.getName migration-file)))))

(defn
  clj-file? [file]
  (if (string? file)
    (.endsWith file ".clj")
    (loading-utils/clj-file? file)))

(defn
#^{ :doc "Returns the names of all of the migrations for this app." }
  all-migrations [migrations-dir]
  (map migration-from-file (filter clj-file? (all-migration-file-names migrations-dir))))

(defn
  migration-namespace-strs [migrations-dir migrate-namespace-prefix]
  (map #(str migrate-namespace-prefix "." %1) (all-migrations migrations-dir)))

(defn
  load-namespace [migration-namespace]
  (when-let [namespace-symbol (symbol migration-namespace)]
    (require namespace-symbol)
    (find-ns namespace-symbol)))

(defn
  migration-namespaces [migrations-dir migrate-namespace-prefix]
  (filter identity (map load-namespace (migration-namespace-strs migrations-dir migrate-namespace-prefix))))