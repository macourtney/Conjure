(ns conjure.core.test.init
  (:import [java.io File])
  (:require [conjure.core.config.environment :as environment]
            [conjure.core.server.server :as server]
            [drift.execute :as drift-execute]))

(defn
  init-tests []
  (when (not (deref server/initialized?))
    (println "Initializing test database.")
    (server/set-mode "test")
    (server/init)
    (drift-execute/migrate nil nil)))