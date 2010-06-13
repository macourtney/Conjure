(ns db.migrate.002-tests-update
  (:use conjure.model.database)
  (:require [clojure.contrib.logging :as logging]))

(defn
#^{:doc "Migrates the database up to version 2."}
  up []
  (logging/debug "Migrating 002-tests-update up"))
  
(defn
#^{:doc "Migrates the database down from version 2."}
  down []
  (logging/debug "Migrating 002-tests-update down"))