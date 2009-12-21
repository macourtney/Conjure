(ns flavors.mysql
  (:import [com.mysql.jdbc.jdbc2.optional MysqlDataSource])
  (:require [clojure.contrib.str-utils :as str-utils]
            [clojure.contrib.sql :as sql]
            [conjure.util.loading-utils :as conjure-loading-utils]
            [conjure.util.string-utils :as conjure-string-utils]))

(defn
#^{:doc "Returns an mysql datasource for a ."}
  create-datasource
    ([connection-url] (create-datasource connection-url nil nil))
    ([connection-url username password]
      (let [mysql-datasource (new MysqlDataSource)]
      (. mysql-datasource setURL connection-url)
      (if (and username password)
        (. mysql-datasource setUser username)
        (. mysql-datasource setPassword password))
      mysql-datasource)))

(defn 
#^{:doc "Returns a map for use in db-config."}
  db-map [db-config]
  (let [
        ;; The name of the production database to use.
        dbname (:dbname db-config)
  
        ;; The name of the JDBC driver to use.
        classname "com.mysql.jdbc.Driver"
        
        ;; The database type.
        subprotocol "mysql"
        
        ;; The database path.
        subname (dbname)
        
        ;; A datasource for the database.
        datasource (create-datasource (format "jdbc:%s:%s" subprotocol subname))]

  (merge db-config {
    :classname classname
    :subprotocol subprotocol
    :subname subname 
    :datasource datasource })))

