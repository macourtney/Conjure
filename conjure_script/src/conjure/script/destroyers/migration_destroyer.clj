(ns conjure.script.destroyers.migration-destroyer
  (:require [clojure.contrib.logging :as logging]
            [drift.destroyer :as destroyer]
            [conjure.core.migration.util :as util]))

(defn
#^{:doc "Destroys a migration file for the migration name given in params."}
  destroy [params]
  (destroyer/destroy-migration-file (first params)))

(defn
#^{:doc "Destroys all of the files created by the migration_generator."}
  destroy-all-dependencies
  ([migration-name]
    (destroyer/destroy-migration-file migration-name)))