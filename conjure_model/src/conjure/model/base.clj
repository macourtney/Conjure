(ns conjure.model.base
  (:require [drift-db.core :as database]
            [drift-db.protocol :as db-protocol]))

(def db (db-protocol/db-map (deref database/drift-db-flavor)))