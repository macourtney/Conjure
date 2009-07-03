(ns conjure.model.database
  (:require [db-config :as db-config]
            [conjure.util.string-utils :as string-utils]))
  
(def conjure-db (db-config/get-db-config))

(defn
#^{:doc "Sets up clojure.contrib.sql"}
  sql-init []
  (let [sql-db-map { :datasource (:datasource conjure-db)
                     :username (:username conjure-db)
                     :password (:password conjure-db)}]
    (def db sql-db-map)))
    
(defn
#^{:doc "Returns the db-flavor from the conjure-db map. If a key is pressent, then this method returns the value of 
that key in the db-flavor"}
  db-flavor 
  ([] (:flavor conjure-db))
  ([flavor-key] (get (db-flavor) flavor-key)))
    
 (defn
#^{:doc "Returns true if the given table exists in the database."}
  table-exists? [table-name]
  ((db-flavor :table-exists) db table-name))
  
(defn
#^{:doc "Runs a select sql statement based on the values in select-map, and returns the results as a vector of maps."}
  sql-find [select-map]
  ((db-flavor :sql-find) db select-map))
  
(defn
#^{:doc "Inserts the given records (a map from column names to values) the table with the given name."}
  insert-into [table-name & records]
  (apply (db-flavor :execute-insert) db table-name records))
  
(defn
#^{:doc "Updates all of the rows which satisfy the given where-params to the values of the given record."}
  update [table-name where-params record]
  ((db-flavor :execute-update) db table-name where-params record))
  
(defn
#^{:doc "Creates a table with the given name and with columns from the given schema-map."}
  create-table [table-name & specs]
  (apply (db-flavor :create-table) db table-name specs))
  
(defmacro
  def-column-spec [type-key]
  (let [spec-name (string-utils/str-keyword type-key)]
    `(defn ~(symbol spec-name)
      ([column#] ((db-flavor ~type-key) column#))
      ([column# mods#] ((db-flavor ~type-key) column# mods#)))))

(def-column-spec :integer)

(def-column-spec :string)

(def-column-spec :text)

(def-column-spec :belongs-to)

(defn
#^{:doc "Returns a new spec describing the id for a table. Use this method with the create-table method."}
  id []
  ((db-flavor :id)))
  
(defn
#^{:doc "Deletes a table with the given name."}
  drop-table [table-name]
  ((db-flavor :drop-table) db table-name))