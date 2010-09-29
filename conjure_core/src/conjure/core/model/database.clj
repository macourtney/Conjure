(ns conjure.core.model.database
  (:require [clojure.contrib.logging :as logging]
            [config.db-config :as db-config]
            [clojure.tools.string-utils :as string-utils]))

(def conjure-db (atom {}))

(defn
  create-db-map [_]
  { :datasource (:datasource @conjure-db)
    :username (:username @conjure-db)
    :password (:password @conjure-db)
    :subprotocol (:subprotocol @conjure-db) })

(def db (atom {}))

(defn
  update-conjure-db [_]
  (db-config/load-config))

(defn init-database []
  (swap! conjure-db update-conjure-db)
  (swap! db create-db-map))

(defn
#^{:doc "Returns the db-flavor from the conjure-db map. If a key is pressent, then this method returns the value of 
that key in the db-flavor"}
  db-flavor 
  ([] (:flavor @conjure-db))
  ([flavor-key] (get (db-flavor) flavor-key)))

(defn
  call-db-fn 
  ([flavor-key db args] (call-db-fn flavor-key (cons db args)))
  ([flavor-key args]
    (let [db-flavor-fn (db-flavor flavor-key)]
      (if db-flavor-fn
        (apply db-flavor-fn args)
        (logging/error (str "Could not find flavor function: " flavor-key))))))

(defmacro
#^{:doc "Given the type-key of a function in the database flavor, define a function named type-key which calls the 
database flavor function with the current db spec and any arguments"}
  def-db-fn [type-key]
  (let [spec-name (string-utils/str-keyword type-key)]
    `(defn ~(symbol spec-name)
      ([& args#] (call-db-fn ~type-key (deref db) args#)))))

(def-db-fn :create-table)

(def-db-fn :delete)

(def-db-fn :describe-table)

(def-db-fn :drop-table)

(def-db-fn :insert-into)

(def-db-fn :sql-find)

(def-db-fn :table-exists?)

(def-db-fn :update)

(defmacro
#^{:doc "Given the type-key of a function in the database flavor, define a function named type-key which calls the 
database flavor function passing any arguments."}
  def-column-spec [type-key]
  (let [spec-name (string-utils/str-keyword type-key)]
    `(defn ~(symbol spec-name)
      ([& args#] (call-db-fn ~type-key args#)))))

(def-column-spec :belongs-to)

(def-column-spec :date)

(def-column-spec :date-time)

(def-column-spec :id)

(def-column-spec :integer)

(def-column-spec :string)

(def-column-spec :text)

(def-column-spec :time-type)

(defmacro
#^{:doc "Given the type-key of a function in the database flavor, define a function named type-key which calls the 
database flavor function passing any arguments."}
  def-db-forward [type-key]
  (let [spec-name (string-utils/str-keyword type-key)]
    `(defn ~(symbol spec-name)
      ([& args#] (call-db-fn ~type-key args#)))))

(def-db-forward :format-date)

(def-db-forward :format-date-time)

(def-db-forward :format-time)