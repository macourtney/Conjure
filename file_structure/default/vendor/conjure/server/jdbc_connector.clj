(ns conjure.server.jdbc-connector
  (:import [java.sql DriverManager])
  (:use [db-config]))

(defn init []
  (. Class forName (:driver (get-db-config))))

(defn connect []
  (. DriverManager getConnection (:connection-url (get-db-config))))