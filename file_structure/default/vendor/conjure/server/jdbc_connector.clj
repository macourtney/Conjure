(ns conjure.server.jdbc-connector
  (:import [java.sql DriverManager])
  (:use [db-config]))

(defn
#^{:doc "Initializes the jdbc driver."}
  init []
  (. Class forName (:driver (get-db-config))))

(defn
#^{:doc "Returns a connection to the database."}
  connect []
  (. DriverManager getConnection (:connection-url (get-db-config))))

(defn
#^{:doc "Executes the given sql string and returns the results as a ResultSet."}
  execute-query [sql-string]
  (. (. (connect) createStatement) executeQuery sql-string))
  
(defn
#^{:doc "Executes and update sql string which does not return a result set."}
  execute-update [sql-string]
  (. (. (connect) createStatement) executeUpdate sql-string))

(defn
#^{:doc "Determines if the given string value exists at the given column index in any row in the given result set, results."}
  exists-in-results [results column-index string-value]
  (let [current-value (. results getString column-index)]
    (if (. string-value equals current-value)
      true
      (if (. results next)
        (exists-in-results results column-index string-value)
        false))))
        
(defn
#^{:doc "Returns the database flavor which is a map of query type symbols to functions. You probably don't want to call this function directly."}
  db-flavor []
  (:flavor (get-db-config)))

(defn
#^{:doc "Returns true if the given table exists in the database."}
  table-exists? [table-name]
  ((:table-exists (db-flavor)) (connect) table-name))
  
(defn
#^{:doc "Runs a select sql statement based on the values in select-map, and returns the results in a ResultSet."}
  sql-find [select-map]
  ((:sql-find (db-flavor)) (connect) select-map))
  
(defn
#^{:doc "Inserts the given values (a sequence of sequences) with the given columns (a sequence) into the table with the given name."}
  insert-into [table-name columns values]
  ((:insert-into (db-flavor)) (connect) table-name columns values))
  
(defn
#^{:doc "Inserts only one value (a sequence) with the given columns (a sequence) into the table with the given name. This method is just a convience method for insert-into."}
  insert-one-into [table-name columns value]
  (insert-into table-name columns (cons value ())))  
  
(defn
#^{:doc "Creates a table with the given name and with columns from the given schema-map."}
  create-table [table-name schema-map]
  ((:create-table (db-flavor)) (connect) table-name schema-map))