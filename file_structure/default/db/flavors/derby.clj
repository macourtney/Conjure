(ns flavors.derby
  (:use [clojure.contrib.str-utils :as str-utils]
        [conjure.util.string-utils :as conjure-string-utils]))
  
(defn
#^{:doc "Executes an sql string and returns the results as a ResultSet."}
  execute-query [connection sql-string]
  ;;(println sql-string)
  (. (. connection createStatement) executeQuery sql-string))
  
(defn
#^{:doc "Executes an update sql string which does not return results."}
  execute-update [connection sql-string]
  ;;(println sql-string)
  (. (. connection createStatement) executeUpdate sql-string))

(defn
#^{:doc "Returns true if the table with the given name exists."}
  table-exists [connection table-name]
  (try
    (let [results (execute-query connection (str "SELECT * FROM " table-name " FETCH FIRST ROW ONLY"))]
      true)
    (catch Exception e false)))
    
(defn
#^{:doc "Runs an sql select statement built from the given select-map. The valid keys are: table - the table to run the select statement on, select - the columns to return, where - the conditions"}
  sql-find [connection select-map]
  (let [table (:table select-map)
        select (or (:select select-map) "*")
        where (:where select-map)]
    (execute-query connection (str "SELECT " select " FROM " table (if where (str " WHERE " where) nil)))))
    
(defn
#^{:doc "Converts the given schema-map into a sequence of sql strings for use in create table or alter table."}
  schema-map-to-sql [schema-map]
  (map (fn [schema-key] (str (conjure-string-utils/str-keyword schema-key) " " (schema-key schema-map))) (keys schema-map)))
    
(defn
#^{:doc "Creates a new table with the given name and with columns based on the given schema map."}
  create-table [connection table-name schema-map]
  (execute-update 
    connection (str "CREATE TABLE " table-name " (" (str-join ", " (schema-map-to-sql schema-map)) ")")))
  
(defn
#^{:doc "Converts a insert value sequence into a string for use in an sql statement"}
  value-as-string [value]
  (str "(" (str-utils/str-join ", " 
    (map 
      (fn [column-value] (if (integer? column-value) column-value (str "'" column-value "'"))) 
      value))
    ")"))
  
(defn
#^{:doc "Converts a sequence of value sequences into a string for use in an sql statement."}
  values-as-string [values]
  (str-join ", " (map value-as-string values)))
  
(defn
#^{:doc "Inserts the given values (a sequence of sequences) with the given columns (a sequence) into the table with the given name."}
  insert-into [connection table-name columns values]
  (execute-update 
    connection 
    (str "INSERT INTO " table-name " (" 
      (str-utils/str-join ", " columns) ") VALUES (" 
      (values-as-string values) ")")))
      
(defn
#^{:doc "Runs an update statement. Set-map is a map containing the parts of the update statement. The supported keys are :set and :where which map to set and where clause strings."}
  update [connection table-name set-map]
  (execute-update
    connection
    (let [where-clause (:where set-map)]
      (str "UPDATE " table-name " SET " (:set set-map) (if where-clause (str " WHERE " where-clause))))))

(defn
#^{:doc "Returns a database flavor for a derby database."}
  flavor []
  {:table-exists table-exists
   :sql-find sql-find
   :create-table create-table
   :insert-into insert-into
   :update update})