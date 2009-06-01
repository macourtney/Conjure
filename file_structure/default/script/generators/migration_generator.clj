(ns generators.migration-generator
  (:use [conjure.util.loading-utils :as loading-utils]))

(defn migration-usage []
  (println "You must supply a migration name (Like migration-name).")
  (println "Usage: ./run.sh script/generate.clj migration <migration name>"))

(defn generate-migration [params]
  (let [migration-name (first params)]
    (if migration-name
      (println (loading-utils/get-classpath-dir-ending-with "db"))
      (migration-usage))))