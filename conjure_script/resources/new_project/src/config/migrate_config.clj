(ns config.migrate-config
  (:require [drift-db.migrate :as drift-db-migrate]
            [conjure.core.server.server :as server]))

(defn migrate-config []
  { :directory "/src/db/migrate"
    :init server/init-args
    :current-version drift-db-migrate/current-version
    :update-version drift-db-migrate/update-version
    :ns-content "\n  (:use drift-db.core)" })