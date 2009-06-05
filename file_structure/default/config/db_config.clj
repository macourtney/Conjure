;; This file is used to configure the database and connection.

(ns db-config
  (:use [flavors.h2 :as h2]))

(defn
#^{:doc "Returns the database config map which is used by jdbc_connector to connect to the database."}
  get-db-config []

  ;; We must create local variables in order to use db-name to create connection-url
  (let [
        ;; The database flavor (SQL syntax type)
        flavor (h2/flavor)

        ;; The name of the JDBC driver to use.
        driver "org.h2.Driver"
        
        ;; The user name to use when connecting to the database.
        user-name "sa"
        
        ;; The password to use when connecting to the database.
        password ""

        ;; The name of the database to use.
        db-name "conjure_test"

        ;; The connection url to pass to the jdbc driver.
        connection-url (str "jdbc:h2:db/data/" db-name)]

    ;; We now return a map of the variables we created above.
    {:flavor flavor
     :driver driver
     :user-name user-name
     :password password
     :db-name db-name
     :connection-url connection-url}))