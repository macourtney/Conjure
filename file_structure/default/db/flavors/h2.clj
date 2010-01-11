(ns flavors.h2
  (:import [org.h2.jdbcx JdbcDataSource]
           [org.h2.jdbc JdbcClob]
           [java.text SimpleDateFormat])
  (:require [clojure.contrib.logging :as logging]
            [clojure.contrib.str-utils :as str-utils]
            [clojure.contrib.sql :as sql]
            [conjure.util.loading-utils :as conjure-loading-utils]
            [conjure.util.string-utils :as conjure-string-utils]))

(defn
#^{:doc "Returns an h2 datasource for a ."}
  create-datasource
    ([connection-url] (create-datasource connection-url nil nil))
    ([connection-url username password]
      (let [h2-datasource (new JdbcDataSource)]
      (. h2-datasource setURL connection-url)
      (if (and username password)
        (. h2-datasource setUser username)
        (. h2-datasource setPassword password))
      h2-datasource)))
      
(defn 
#^{:doc "Returns a map for use in db-config."}
  db-map [db-config]
  (let [
        ;; The name of the production database to use.
        dbname (:dbname db-config)
  
        ;; The name of the JDBC driver to use.
        classname "org.h2.Driver"
        
        ;; The database type.
        subprotocol "h2"
        
        ;; The database path.
        subname (str "db/data/" dbname)
        
        ;; A datasource for the database.
        datasource (create-datasource (format "jdbc:%s:%s" subprotocol subname))]

  (merge db-config {
    :classname classname
    :subprotocol subprotocol
    :subname subname 
    :datasource datasource })))
    
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
#^{ :doc "Executes an sql string and returns the results as a sequence of maps." }
  execute-query [db-spec sql-vector]
  (do
    (logging/debug (str "Executing query: " sql-vector))
    (sql/with-connection db-spec
      (sql/with-query-results rows sql-vector
        (doall (map clean-row rows))))))
  
(defn
#^{:doc "Returns the given key or string as valid table name. Basically turns 
any keyword into a string, and replaces dashes with underscores."}
  table-name [table]
  (conjure-loading-utils/dashes-to-underscores (conjure-string-utils/str-keyword table)))
  
(defn
#^{:doc "Runs an update given the table, where-params and a record.

  table - The name of the table to update.
  where-params - The parameters to test for.
  record - A map from strings or keywords (identifying columns) to updated values."}
  update [db-spec table where-params record]
  (do
    (logging/debug (str "Update table: " table " where: " where-params " record: " record))
    (sql/with-connection db-spec
      (sql/update-values (table-name table) where-params record))))

(defn
#^{:doc "Runs an insert given the table, and a set of records.

  table - The name of the table to update.
  records - A map from strings or keywords (identifying columns) to updated values."}
  insert-into [db-spec table & records]
  (do
    (logging/debug (str "insert into: " table " records: " records))
    (sql/with-connection db-spec
      (apply sql/insert-records (table-name table) records))))


(defn
#^{:doc "Returns true if the table with the given name exists."}
  table-exists? [db-spec table]
  (try
    (let [results (execute-query db-spec [(str "SELECT * FROM " (table-name table) " LIMIT 1")])]
      true)
    (catch Exception e false)))
    
(defn
#^{:doc "Runs an sql select statement built from the given select-map. The valid keys are: table - the table to run the select statement on, select - the columns to return, where - the conditions"}
  sql-find [db-spec select-map]
  (let [table (:table select-map)
        select-clause (or (:select select-map) "*")
        where-clause (:where select-map)
        limit-clause (:limit select-map)]
    (execute-query db-spec 
      [(str "SELECT " select-clause " FROM " (table-name table)
           (if where-clause (str " WHERE " where-clause) nil) 
           (if limit-clause (str " LIMIT " limit-clause) nil))])))

(defn
#^{:doc "Creates a new table with the given name and with columns based on the given specs."}
  create-table [db-spec table & specs]
  (do
    (logging/debug (str "Create table: " table " with specs: " specs))
    (sql/with-connection db-spec
      (apply sql/create-table (table-name table) specs))))

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

(defn
#^{:doc "Returns a new spec describing an integer with the given column and spec mods map. Use this method with the 
create-table method.

  Curently supported values for mods:
    :not-null - If the value of this key resolves to true, then add this column will be forced to be not null.
    :primary-key - If true, then make this column the primary key."}
  integer
  ([column] (integer column {})) 
  ([column mods]
      (concat [(column-name column) "INT"] (not-null-mod mods) (auto-increment-mod mods) (primary-key-mod mods))))
    
(defn
#^{:doc "Returns a new spec describing the id for a table. Use this method with the create-table method."}
  id []
  (integer "id" { :not-null true, :primary-key true, :auto-increment true }))
  
(defn
#^{:doc "Returns a new spec describing a text with the given column and spec mods map. Use this method with the create-table method.

  Curently supported values for mods is exactly the same as integer."}
  belongs-to
  ([model] (belongs-to model {}))
  ([model mods]
    (integer (str (column-name model) "_id") mods)))
  
(defn
#^{:doc "Returns a new spec describing a string with the given column and spec mods map. Use this method with the create-table method.

  Curently supported values for mods:
    :length - The length of the varchar, if not present then the varchar defaults to 255.
    :not-null - If the value of this key resolves to true, then add this column will be forced to be not null.
    :primary-key - If true, then make this column the primary key."}
  string
  ([column] (string column { :length 255 }))
  ([column mods]
    (let [length (get mods :length 255)
          varchar (str "VARCHAR(" length ")")]
      (concat [(column-name column) varchar] (not-null-mod mods) (primary-key-mod mods)))))
      
(defn
#^{:doc "Returns a new spec describing a text with the given column and spec mods map. Use this method with the 
create-table method.

  Curently supported values for mods:
    None"}
  text
  ([column] (text column {}))
  ([column mods]
      [(column-name column) "TEXT"]))
      
(defn
#^{ :doc "Returns a new spec describing a date with the given column and spec mods map. Use this method with the 
create-table method.

  Curently supported values for mods:
    None" }
  date
  ([column] (date column {}))
  ([column mods]
    [(column-name column) "DATE"]))

(defn
#^{ :doc "Returns a new spec describing a time with the given column and spec mods map. Use this method with the 
create-table method.

  Curently supported values for mods:
    None" }
  time-type
  ([column] (time-type column {}))
  ([column mods]
    [(column-name column) "TIME"]))

(defn
#^{ :doc "Returns a new spec describing a date time with the given column and spec mods map. Use this method with the 
create-table method.

  Curently supported values for mods:
    None" }
  date-time
  ([column] (date-time column {}))
  ([column mods]
    [(column-name column) "TIMESTAMP"]))
    
(defn
#^{:doc "Deletes the table with the given name."}
  drop-table [db-spec table]
  (do
    (logging/debug (str "Drop table: " table))
    (sql/with-connection db-spec
      (sql/drop-table (table-name table)))))

(defn
#^{:doc "Deletes rows from the table with the given name."}
  delete [db-spec table where]
  (do
    (logging/debug (str "Delete from " table " where " where))
    (sql/with-connection db-spec
      (sql/delete-rows (table-name table) where))))
    
(defn
#^{ :doc "Returns the string value of the given date for use in the database." }
  format-date [date]
  (. (new SimpleDateFormat "yyyy-MM-dd") format date))

(defn
#^{ :doc "Returns the string value of the given date as a date time for use in the database." }
  format-date-time [date]
  (. (new SimpleDateFormat "yyyy-MM-dd HH:mm:ss") format date))
    
(defn
#^{ :doc "Returns the string value of the given date as a time for use in the database." }
  format-time [date]
  (. (new SimpleDateFormat "HH:mm:ss") format date))
  
(defn
#^{:doc "Returns a database flavor for a derby database."}
  flavor []
  { :db-map db-map
    :execute-query execute-query
    :update update
    :insert-into insert-into
    :table-exists? table-exists?
    :sql-find sql-find
    :create-table create-table
    :drop-table drop-table
    :delete delete
    :integer integer
    :id id
    :string string
    :text text
    :date date
    :time-type time-type
    :date-time date-time
    :belongs-to belongs-to
    :format-date format-date
    :format-date-time format-date-time
    :format-time format-time
  })