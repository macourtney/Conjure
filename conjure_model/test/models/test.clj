(ns models.test
  (:require [test-helper :as test-helper])
  (:use clj-record.boot))

(test-helper/init-server #(identity true))

(use 'conjure.core.model.base)

(clj-record.core/init-model)