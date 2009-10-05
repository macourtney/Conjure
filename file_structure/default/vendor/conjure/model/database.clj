(ns conjure.model.database
  (:require [db-config :as db-config]
            [conjure.util.string-utils :as string-utils]))

(def conjure-db (ref nil))

(defn
  create-db-map []
  { :datasource (:datasource @conjure-db)
    :username (:username @conjure-db)
    :password (:password @conjure-db)
    :subprotocol (:subprotocol @conjure-db) })
    
(defn
#^{:doc "Ensures the conjure-db ref is set before returning it's value."}
  ensure-conjure-db []
  (if @conjure-db
    @conjure-db
    (do
      (dosync (alter conjure-db (fn [_] (db-config/load-config))))
      (def db (create-db-map))
      @conjure-db)))
    
(defn
#^{:doc "Returns the db-flavor from the conjure-db map. If a key is pressent, then this method returns the value of 
that key in the db-flavor"}
  db-flavor 
  ([] (:flavor (ensure-conjure-db)))
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

(def-db-fn :delete)
   
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