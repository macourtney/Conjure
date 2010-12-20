(ns conjure.core.db.flavors.google-app-engine
  (:require [appengine.datastore.entities :as entities]
            [appengine.datastore.protocols :as protocols]
            [appengine.datastore.query :as datastore-query]
            [clojure.contrib.logging :as logging] 
            [clojure.tools.string-utils :as string-utils]
            [conjure.core.db.flavors.protocol :as flavor-protocol])
  (:import [com.google.appengine.api.datastore Query$FilterOperator]
           [conjure.core.db.flavors.protocol Flavor])
  (use appengine.datastore.query))

(defn-
  convert-operator [operator]
  (cond
    (instance? Query$FilterOperator operator) operator
    (string? operator) (Query$FilterOperator/valueOf operator)
    true (throw (RuntimeException. (str "Unknown operator type: " (class operator) ". Operator must be a string or FilterOperator.")))))

(deftype H2Flavor [username password dbname]
  Flavor
  (db-map [flavor]
    { :flavor flavor })
  
  (execute-query [flavor sql-vector]
    (do
      (logging/debug (str "Executing query: " query))
      (protocols/execute query)))
  
  (update [flavor _ _ entity]
    (do
      (logging/debug (str "Update entity: " entity))
      (protocols/save-entity entity)))

  (insert-into [flavor _ entities]
    (do
      (logging/debug (str "insert entities: " entities))
      (doseq [entity entities]
        (flavor-protocol/update flavor entity))))

  (table-exists? [flavor _]
    true)
  
  (sql-find [flavor query]
    (protocols/execute query))

  (create-table [flavor table specs])

  (drop-table [flavor kind]
    (do
      (logging/debug (str "Drop kind: " kind))
      (doall (protocols/delete-entity (protocols/execute (datastore-query/query kind))))))

  (describe-table [flavor kind]
    (do
      (logging/debug (str "Describe kind: " kind))
      (reduce
        #(conj %1 { :field (string-utils/str-keyword %2) }) 
        [] 
        (keys (entities/deserialize-entity (first (protocols/execute (datastore-query/query kind))))))))

  (delete [flavor kind filters]
    (do
      (logging/debug (str "Delete from " kind " where " filters))
      (doall
        (protocols/delete-entity
          (protocols/execute
            (doto (datastore-query/query kind)
              (when filters
                (doseq [filter-map filters]
                  (.addFilter (:property-name filter-map) (convert-operator (:operator filter-map)) (:value filter-map)))))))))
    (do
      (logging/debug (str "Delete from " table " where " where))
      (sql/with-connection (flavor-protocol/db-map flavor)
        (sql/delete-rows (table-name table) where))))
  
  (integer [flavor column] (flavor-protocol/integer flavor column {}) )
  (integer [_ column mods] [])

  (id [flavor] [])

  (string [flavor column] (flavor-protocol/string flavor column { :length 255 }))
  (string [_ column mods] [])

  (text [flavor column] (flavor-protocol/text flavor column {}))
  (text [_ column mods] [])
  
  (date [flavor column] (flavor-protocol/date flavor column {}))
  (date [_ column mods] [])
  
  (time-type [flavor column] (flavor-protocol/time-type flavor column {}))
  (time-type [_ column mods] [])

  (date-time [flavor column] (flavor-protocol/date-time flavor column {}))
  (date-time [_ column mods] [])

  (belongs-to [flavor model] (flavor-protocol/belongs-to flavor model {}))
  (belongs-to [flavor model mods] [])

  (format-date [flavor date] date)

  (format-date-time [flavor date] date)

  (format-time [flavor date]
    date))