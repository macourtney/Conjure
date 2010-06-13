(ns db.migrate.001-create-tests
  (:use conjure.model.database)
  (:require [clojure.contrib.logging :as logging]))

(defn
#^{:doc "Migrates the database up to version 1."}
  up []
  (logging/debug "Migrating 001-create-tests up"))
  
(defn
#^{:doc "Migrates the database down from version 1."}
  down []
  (logging/debug "Migrating 001-create-tests down"))