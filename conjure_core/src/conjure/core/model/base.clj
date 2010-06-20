(ns conjure.core.model.base
  (:require [conjure.core.model.database :as database]))

(def db (deref database/db))