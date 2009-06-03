;; This file is used to configure the database and connection.

(ns db-config)

(defn get-db-config []

  ;; We must create local variables in order to use db-name to create connection-url
  (let [

        ;; The name of the JDBC driver to use.
        driver "org.apache.derby.jdbc.EmbeddedDriver"

        ;; The name of the database to use.
        db-name "conjure_test"

        ;; The connection url to pass to the jdbc driver.
        connection-url (str "jdbc:derby:" db-name ";create=true")]

    ;; We now return a map of the variables we created above.
    {:driver driver
     :db-name db-name
     :connection-url connection-url}))