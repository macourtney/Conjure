(ns conjure.server.jdbc-connector
  (:import [java.sql DriverManager])
  (:use [db-config]))

(defn init []
  (. Class forName (:driver (get-db-config))))

(defn connect []
  (. DriverManager getConnection (:connection-url (get-db-config))))

(defn execute-query [sql-string]
  (. (. (connect) createStatement) executeQuery sql-string))

(defn exists-in-results [results column-index string-value]
  (let [current-value (. results getString column-index)]
    (if (. string-value equals current-value)
      true
      (if (. results next)
        (exists-in-results results column-index string-value)
        false))))

(defn table-exists? [table-name]
  (let [results (execute-query (str "SELECT * FROM " table-name " FETCH FIRST ROW ONLY"))]
    (if (. results first)
      (exists-in-results results 0 table-name)
      false)))