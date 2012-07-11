(ns conjure.test.init
  (:import [java.io File])
  (:require [conjure.config.environment :as environment]
            [conjure.server.server :as server]
            [drift.execute :as drift-execute]))

(defn
  init-tests []
  (when (not (deref server/initialized?))
    (println "Initializing test database.")
    (server/set-mode "test")
    (server/init)
    (drift-execute/migrate nil nil)))