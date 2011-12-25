(ns models.test
  (:require [conjure.core.server.server :as server])
  (:use clj-record.boot))

(server/init)

(use 'conjure.core.model.base)

(clj-record.core/init-model)