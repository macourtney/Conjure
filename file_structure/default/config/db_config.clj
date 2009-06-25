;; This file is used to configure the database and connection.

(ns db-config
  (:require [flavors.h2 :as h2]))

(defn
#^{:doc "Returns the database config map which is used by jdbc_connector to connect to the database."}
  get-db-config []

  ;; We must create local variables in order to use db-name to create the connection to the database.
  (let [
        ;; The database flavor (SQL syntax type)
        flavor (h2/flavor)

        ;; The name of the database to use.
        dbname "conjure_test"

        ;; The user name to use when connecting to the database.
        username "sa"
        
        ;; The password to use when connecting to the database.
        password ""]

    ;; We now return a map of the variables we created above.
    (merge ((flavor :db-map) dbname)
      {:flavor flavor
       :username username
       :password password
       :dbname dbname})))