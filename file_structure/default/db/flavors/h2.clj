(ns flavors.h2
  (:import [org.h2.jdbcx JdbcDataSource])
  (:require [clojure.contrib.str-utils :as str-utils]
            [clojure.contrib.sql :as sql]
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
  db-map [dbname]
  (let [
        ;; The name of the JDBC driver to use.
        classname "org.h2.Driver"
        
        ;; The database type.
        subprotocol "h2"
        
        ;; The database path.
        subname (str "db/data/" dbname)
        
        ;; A datasource for the database.
        datasource (create-datasource (format "jdbc:%s:%s" subprotocol subname))]

  { :classname classname
    :subprotocol subprotocol
    :subname subname 
    :datasource datasource}))
  
(defn
#^{:doc "Executes an sql string and returns the results as a ResultSet."}
  execute-query [db-spec sql-vector]
  ;;(println sql-string)
  (sql/with-connection db-spec
    (sql/with-query-results rows sql-vector
      (doall rows))))
  ;(. (. connection createStatement) executeQuery sql-string))
  
(defn
#^{:doc "Runs an update given the table, where-params and a record.

  table - The name of the table to update.
  where-params - The parameters to test for.
  record - A map from strings or keywords (identifying columns) to updated values."}
  execute-update [db-spec table where-params record]
  ;;(println sql-string)
  (sql/with-connection db-spec
    (sql/update-values table where-params record)))

(defn
#^{:doc "Runs an insert given the table, and a set of records.

  table - The name of the table to update.
  records - A map from strings or keywords (identifying columns) to updated values."}
  execute-insert [db-spec table & records]
  ;;(println sql-string)
  (sql/with-connection db-spec
    (apply sql/insert-records table records)))


(defn
#^{:doc "Returns true if the table with the given name exists."}
  table-exists [db-spec table-name]
  (try
    (let [results (execute-query db-spec [(str "SELECT * FROM " table-name " LIMIT 1")])]
      true)
    (catch Exception e false)))
    
(defn
#^{:doc "Runs an sql select statement built from the given select-map. The valid keys are: table - the table to run the select statement on, select - the columns to return, where - the conditions"}
  sql-find [db-spec select-map]
  (let [table-name (:table select-map)
        select-clause (or (:select select-map) "*")
        where-clause (:where select-map)
        limit-clause (:limit select-map)]
    (execute-query db-spec 
      [(str "SELECT " select-clause " FROM " table-name
           (if where-clause (str " WHERE " where-clause) nil) 
           (if limit-clause (str " LIMIT " limit-clause) nil))])))

(defn
#^{:doc "Creates a new table with the given name and with columns based on the given specs."}
  create-table [db-spec table & specs]
  (sql/with-connection db-spec
    (apply sql/create-table table specs))
  ;(execute-update 
  ;  connection (str "CREATE TABLE " table-name " (" (str-join ", " (schema-map-to-sql schema-map)) ")"))
    )
    
(defn
#^{:doc "Returns a new spec describing an integer with the given column and spec mods map. Use this method with the 
create-table method.

  Curently supported values for mods:
    :not-null - If the value of this key resolves to true, then add this column will be forced to be not null.
    :primary-key - If true, then make this column the primary key."}
  integer
  ([column] (integer column {})) 
  ([column mods]
    (let [spec [column "INT"]
          not-null (if (:not-null mods) ["NOT NULL"] [])
          primary-key (if (:primary-key mods) ["PRIMARY KEY"] [])
          output (concat spec not-null primary-key)]
      output)))
    
(defn
#^{:doc "Returns a new spec describing the id for a table. Use this method with the create-table method."}
  id []
  (integer "id" ))
    
(defn
#^{:doc "Deletes the table with the given name."}
  drop-table [db-spec table]
  (sql/with-connection db-spec
    (sql/drop-table table)))
  
;(defn
;#^{:doc "Converts a insert value sequence into a string for use in an sql statement"}
;  value-as-string [value]
;  (str "(" (str-utils/str-join ", " 
;    (map 
;      (fn [column-value] (if (integer? column-value) column-value (str "'" column-value "'"))) 
;      value))
;    ")"))
  
;(defn
;#^{:doc "Converts a sequence of value sequences into a string for use in an sql statement."}
;  values-as-string [values]
;  (if (empty? (rest values))
;    (value-as-string (first values))
;    (str "(" (str-join ", " (map value-as-string values)) ")")))
  
;(defn
;#^{:doc "Inserts the given values (a sequence of sequences) with the given columns (a sequence) into the table with the given name."}
;  insert-into [connection table-name columns values]
;  (execute-update 
;    connection 
;    (str "INSERT INTO " table-name " (" 
;      (str-utils/str-join ", " columns) ") VALUES " 
;      (values-as-string values))))
      
;(defn
;#^{:doc "Runs an update statement. Set-map is a map containing the parts of the update statement. The supported keys are :set and :where which map to set and where clause strings."}
;  update [connection table-name set-map]
;  (execute-update
;    connection
;    (let [where-clause (:where set-map)]
;      (str "UPDATE " table-name " SET " (:set set-map) (if where-clause (str " WHERE " where-clause))))))

(defn
#^{:doc "Returns a database flavor for a derby database."}
  flavor []
  {:db-map db-map
   :execute-query execute-query
   :execute-update execute-update
   :execute-insert execute-insert
   :table-exists table-exists
   :sql-find sql-find
   :integer integer
   :create-table create-table
   :id id
   :drop-table drop-table
  })