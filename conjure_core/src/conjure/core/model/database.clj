(ns conjure.core.model.database
  (:require [clojure.contrib.logging :as logging]
            [clojure.tools.string-utils :as string-utils]
            [config.db-config :as db-config]
            [conjure.core.db.flavors.protocol :as flavor-protocol]))

(def conjure-flavor (atom nil))

(defn
  create-db-map [_]
  (select-keys (flavor-protocol/db-map @conjure-flavor) [:datasource :username :password :subprotocol]))

(def db (atom {}))

(defn
  update-conjure-flavor [_]
  (db-config/load-config))

(defn init-database []
  (swap! conjure-flavor update-conjure-flavor)
  (swap! db create-db-map))

(defmacro
#^{:doc "Given the type-key of a function in the database flavor, define a function named type-key which calls the 
database flavor function with the current db spec and any arguments"}
  def-db-fn [type-key]
  (let [spec-name (string-utils/str-keyword type-key)]
    `(defn ~(symbol spec-name)
      ([& args#] (apply ~(symbol (str "flavor-protocol/" spec-name)) @conjure-flavor args#)))))

(defn create-table [table & specs]
  (flavor-protocol/create-table @conjure-flavor table specs))

(def-db-fn :delete)

(def-db-fn :describe-table)

(def-db-fn :drop-table)

(defn insert-into [table & records]
  (flavor-protocol/insert-into @conjure-flavor table records))

;(def-db-fn :insert-into)

(def-db-fn :sql-find)

(def-db-fn :table-exists?)

(def-db-fn :update)

(def-db-fn :belongs-to)

(def-db-fn :date)

(def-db-fn :date-time)

(def-db-fn :id)

(def-db-fn :integer)

(def-db-fn :string)

(def-db-fn :text)

(def-db-fn :time-type)

(def-db-fn :format-date)

(def-db-fn :format-date-time)

(def-db-fn :format-time)