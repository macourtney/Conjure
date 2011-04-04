;; This file is used to configure the database and connection.

(ns config.db-config
  (:require [clojure.contrib.java-utils :as java-utils]
            [conjure.core.config.environment :as environment]
            [conjure.core.db.flavors.h2 :as h2])
  (:import [conjure.core.db.flavors.h2 H2Flavor]))

(defn dbname [environment]
  (cond
     ;; The name of the production database to use.
     (= environment :production) "conjure_production"

     ;; The name of the development database to use.
     (= environment :development) "conjure_development"

     ;; The name of the test database to use.
     (= environment :test) "conjure_test"))

(defn
#^{:doc "Returns the database flavor which is used by Conjure to connect to the database."}
  create-flavor 
  ([] (create-flavor :production))
  ([environment]
    (H2Flavor.
      ;; The user name to use when connecting to the database.
      "conjure"

      ;; The password to use when connecting to the database.
      "conjure"

      ;; Calculates the database to use.
      (dbname environment))))
            
(defn
  load-config []
  (let [environment (environment/environment-name)
        flavor (create-flavor (keyword environment))]
    (if flavor
      flavor
      (throw (new RuntimeException (str "Unknown environment: " environment ". Please check your conjure.environment system property."))))))
      