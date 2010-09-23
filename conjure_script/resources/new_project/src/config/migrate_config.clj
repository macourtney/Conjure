(ns config.migrate-config
  (:require [conjure.core.migration.util :as util]))

(defn migrate-config []
  { :directory "/src/db/migrate"
    :init util/init
    :current-version util/current-version
    :update-version util/update-version
    :ns-content "\n  (:use conjure.core.model.database)"
    :migration-namespaces util/migration-namespaces })