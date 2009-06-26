(ns conjure.model.base
  (:require [conjure.model.database :as database]))

(defn
#^{:doc "Sets up clojure.contrib.sql"}
  sql-init []
    (database/sql-init)
    (def db database/db))