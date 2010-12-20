(ns conjure.core.db.flavors.h2
  (:require [clojure.contrib.logging :as logging]
            [clojure.contrib.str-utils :as str-utils]
            [clojure.contrib.sql :as sql]
            [clojure.tools.loading-utils :as conjure-loading-utils]
            [clojure.tools.string-utils :as conjure-string-utils]
            [conjure.core.db.flavors.protocol :as flavor-protocol])
  (:import [conjure.core.db.flavors.protocol Flavor]
           [java.text SimpleDateFormat]
           [org.h2.jdbcx JdbcDataSource]
           [org.h2.jdbc JdbcClob]))

(defn
#^{:doc "Returns an h2 datasource for a ."}
  create-datasource
    ([connection-url] (create-datasource connection-url nil nil))
    ([connection-url username password]
      (let [h2-datasource (new JdbcDataSource)]
        (. h2-datasource setURL connection-url)
        (when (and username password)
          (. h2-datasource setUser username)
          (. h2-datasource setPassword password))
        h2-datasource)))

(defn-
#^{ :doc "Cleans up the given value, loading any clobs into memory." }
  clean-value [value]
  (if (and value (instance? JdbcClob value))
    (.getSubString value 1 (.length value))
    value))
  
(defn-
#^{ :doc "Cleans up the given row, loading any clobs into memory." }
  clean-row [row]
  (reduce 
    (fn [new-map pair] 
        (assoc new-map (first pair) (clean-value (second pair))))
    {} 
    row))

(defn
#^{:doc "Returns the given key or string as valid table name. Basically turns any keyword into a string, and replaces
dashes with underscores."}
  table-name [table]
  (conjure-loading-utils/dashes-to-underscores (conjure-string-utils/str-keyword table)))

(defn
#^{:doc "Returns the not null spec vector from the given mods map."}
  not-null-mod [mods]
  (if (:not-null mods) ["NOT NULL"] []))

(defn
#^{:doc "Returns the primary key spec vector from the given mods map."}
  primary-key-mod [mods]
  (if (:primary-key mods) ["PRIMARY KEY"] []))

(defn
#^{:doc "Returns the primary key spec vector from the given mods map."}
  auto-increment-mod [mods]
  (if (:auto-increment mods) ["AUTO_INCREMENT"] []))

(defn
#^{:doc "Returns the given key or string as valid column name. Basically turns 
any keyword into a string, and replaces dashes with underscores."}
  column-name [column]
  (conjure-loading-utils/dashes-to-underscores (conjure-string-utils/str-keyword column)))

(deftype H2Flavor [username password dbname]
  Flavor
  (db-map [flavor]
    (let [subprotocol "h2"

          subname (str "db/data/" dbname)]
  
      { :flavor flavor

        ;; The name of the JDBC driver to use.
        :classname "org.h2.Driver"
        
        ;; The database type.
        :subprotocol subprotocol
  
        ;; The database path.
        :subname subname
  
        ;; A datasource for the database.
        :datasource (create-datasource (format "jdbc:%s:%s" subprotocol subname))

        ;; The user name to use when connecting to the database.
        :username username

        ;; The password to use when connecting to the database.
        :password password }))
  
  (execute-query [flavor sql-vector]
    (do
      (logging/debug (str "Executing query: " sql-vector))
      (sql/with-connection (flavor-protocol/db-map flavor)
        (sql/with-query-results rows sql-vector
          (doall (map clean-row rows))))))
  
  (update [flavor table where-params record]
    (do
      (logging/debug (str "Update table: " table " where: " where-params " record: " record))
      (sql/with-connection (flavor-protocol/db-map flavor)
        (sql/update-values (table-name table) where-params record))))

  (insert-into [flavor table records]
    (do
      (logging/debug (str "insert into: " table " records: " records))
      (sql/with-connection (flavor-protocol/db-map flavor)
        (apply sql/insert-records (table-name table) records))))

  (table-exists? [flavor table]
    (try
      (let [results (flavor-protocol/execute-query flavor [(str "SELECT * FROM " (table-name table) " LIMIT 1")])]
        true)
      (catch Exception e false)))
  
  (sql-find [flavor select-map]
    (let [table (:table select-map)
          select-clause (or (:select select-map) "*")
          where-clause (:where select-map)
          limit-clause (:limit select-map)]
      (flavor-protocol/execute-query flavor 
        [(str "SELECT " select-clause " FROM " (table-name table)
             (when where-clause (str " WHERE " where-clause)) 
             (when limit-clause (str " LIMIT " limit-clause)))])))

  (create-table [flavor table specs]
    (do
      (logging/debug (str "Create table: " table " with specs: " specs))
      (sql/with-connection (flavor-protocol/db-map flavor)
        (apply sql/create-table (table-name table) specs))))

  (drop-table [flavor table]
    (do
      (logging/debug (str "Drop table: " table))
      (sql/with-connection (flavor-protocol/db-map flavor)
        (sql/drop-table (table-name table)))))

  (describe-table [flavor table]
    (do
      (logging/debug (str "Describe table: " table))
      (flavor-protocol/execute-query flavor [(str "SHOW COLUMNS FROM " (table-name table))])))

  (delete [flavor table where]
    (do
      (logging/debug (str "Delete from " table " where " where))
      (sql/with-connection (flavor-protocol/db-map flavor)
        (sql/delete-rows (table-name table) where))))
  
  (integer [flavor column] (flavor-protocol/integer flavor column {}) )
  (integer [_ column mods]
    (concat [(column-name column) "INT"] (not-null-mod mods) (auto-increment-mod mods) (primary-key-mod mods)))

  (id [flavor]
    (flavor-protocol/integer flavor "id" { :not-null true, :primary-key true, :auto-increment true }))

  (string [flavor column] (flavor-protocol/string flavor column { :length 255 }))
  (string [_ column mods]
    (let [length (get mods :length 255)
          varchar (str "VARCHAR(" length ")")]
      (concat [(column-name column) varchar] (not-null-mod mods) (primary-key-mod mods))))

  (text [flavor column] (flavor-protocol/text flavor column {}))
  (text [_ column mods] [(column-name column) "TEXT"])
  
  (date [flavor column] (flavor-protocol/date flavor column {}))
  (date [_ column mods] [(column-name column) "DATE"])
  
  (time-type [flavor column] (flavor-protocol/time-type flavor column {}))
  (time-type [_ column mods] [(column-name column) "TIME"])

  (date-time [flavor column] (flavor-protocol/date-time flavor column {}))
  (date-time [_ column mods] [(column-name column) "TIMESTAMP"])

  (belongs-to [flavor model] (flavor-protocol/belongs-to flavor model {}))
  (belongs-to [flavor model mods] (flavor-protocol/integer flavor (str (column-name model) "_id") mods))

  (format-date [flavor date]
    (. (new SimpleDateFormat "yyyy-MM-dd") format date))

  (format-date-time [flavor date]
    (. (new SimpleDateFormat "yyyy-MM-dd HH:mm:ss") format date))

  (format-time [flavor date]
    (. (new SimpleDateFormat "HH:mm:ss") format date)))