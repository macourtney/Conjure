;; This file is used to configure the database and connection.

(ns db-config
  (:use [flavors.derby :as derby]))

(defn
#^{:doc "Returns the database config map which is used by jdbc_connector to connect to the database."}
  get-db-config []

  ;; We must create local variables in order to use db-name to create connection-url
  (let [
        ;; The database flavor (SQL syntax type)
        flavor (derby/flavor)

        ;; The name of the JDBC driver to use.
        driver "org.apache.derby.jdbc.EmbeddedDriver"

        ;; The name of the database to use.
        db-name "conjure_test"

        ;; The connection url to pass to the jdbc driver.
        connection-url (str "jdbc:derby:" db-name ";create=true")]

    ;; We now return a map of the variables we created above.
    {:flavor flavor
     :driver driver
     :db-name db-name
     :connection-url connection-url}))