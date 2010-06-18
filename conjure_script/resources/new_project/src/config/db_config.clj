;; This file is used to configure the database and connection.

(ns config.db-config
  (:require [clojure.contrib.java-utils :as java-utils]
            [conjure.core.config.environment :as environment]
            [conjure.core.db.flavors.h2 :as h2]))

(defn
#^{:doc "Returns the database config map which is used by jdbc_connector to connect to the database."}
  create-db-config 
  ([] (create-db-config :production))
  ([environment]
    (let [
          ;; The default settings for all environments
          base-config
            {
              ;; The database flavor (SQL syntax type)
              :flavor (h2/flavor)
          
              ;; The user name to use when connecting to the database.
              :username "conjure"
            
              ;; The password to use when connecting to the database.
              :password "conjure" }]

      (cond
        (= environment :production)
          (merge base-config { 
            ;; The name of the production database to use.
            :dbname "conjure_production" })
            
        (= environment :development)
          (merge base-config { 
            ;; The name of the development database to use.
            :dbname "conjure_development" })
            
        (= environment :test)
          (merge base-config { 
            ;; The name of the test database to use.
            :dbname "conjure_test" })))))
            
(defn
  load-config []
  (let [environment (environment/environment-name)
        base-config (create-db-config (keyword environment))]
    (if base-config
      (((:flavor base-config) :db-map) base-config)
      (throw (new RuntimeException (str "Unknown environment: " environment ". Please check your conjure.environment system property."))))))
      