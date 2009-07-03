(ns conjure.model.database
  (:require [db-config :as db-config]
            [conjure.util.string-utils :as string-utils]))

(defn
#^{:doc "Initializes the database config information."}
  init-sql []
  (let [new-db-config (db-config/load-config)
        sql-db-map { :datasource (:datasource new-db-config)
                     :username (:username new-db-config)
                     :password (:password new-db-config)}]
    (def conjure-db new-db-config)
    (def db sql-db-map)))
    
(defn
#^{:doc "Returns the db-flavor from the conjure-db map. If a key is pressent, then this method returns the value of 
that key in the db-flavor"}
  db-flavor 
  ([] (:flavor conjure-db))
  ([flavor-key] (get (db-flavor) flavor-key)))
  
(defmacro
#^{:doc "Given the type-key of a function in the database flavor, define a function named type-key which calls the 
database flavor function with the current db spec and any arguments"}
  def-db-fn [type-key]
  (let [spec-name (string-utils/str-keyword type-key)]
    `(defn ~(symbol spec-name)
      ([& args#] (apply (db-flavor ~type-key) db args#)))))
    
(def-db-fn :table-exists?)

(def-db-fn :sql-find)

(def-db-fn :insert-into)

(def-db-fn :update)

(def-db-fn :create-table)

(def-db-fn :drop-table)
   
(defmacro
#^{:doc "Given the type-key of a function in the database flavor, define a function named type-key which calls the 
database flavor function passing any arguments."}
  def-column-spec [type-key]
  (let [spec-name (string-utils/str-keyword type-key)]
    `(defn ~(symbol spec-name)
      ([& args#] (apply (db-flavor ~type-key) args#)))))

(def-column-spec :integer)

(def-column-spec :string)

(def-column-spec :text)

(def-column-spec :belongs-to)

(def-column-spec :id)
  
(defn
#^{:doc "Deletes a table with the given name."}
  drop-table [table-name]
  ((db-flavor :drop-table) db table-name))