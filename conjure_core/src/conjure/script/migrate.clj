(ns conjure.script.migrate
  (:require [drift.execute :as execute]))

(defn
  run [args]
  (execute/run args))