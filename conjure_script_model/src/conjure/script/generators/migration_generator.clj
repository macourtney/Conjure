(ns conjure.script.generators.migration-generator
  (:import [java.io File])
  (:require [drift.generator :as generator]))

(defn
#^{ :doc "Generates a migration file for the migration name given in params." }
  generate [params]
  (generator/generate-migration-file (first params)))