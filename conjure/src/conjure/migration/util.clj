(ns conjure.migration.util
  (:require [clojure.contrib.seq-utils :as seq-utils]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.file-utils :as file-utils]))

(def schema-info-table "schema_info")
(def version-column :version)

(defn 
#^{:doc "Finds the db directory which contains all of the files for updating the schema for the database."}
  find-db-directory []
  (loading-utils/get-classpath-dir-ending-with "db"))

(defn
#^{:doc "Finds the migrate directory."}
  find-migrate-directory []
  (file-utils/find-directory (find-db-directory) "migrate"))
  
(defn 
#^{:doc "Returns all of the migration files as a collection."}
  all-migration-files
  ([] (all-migration-files (find-migrate-directory)))
  ([migrate-directory]
    (if migrate-directory
      (filter 
        (fn [migrate-file] 
          (re-find #"^[0-9]+_.+\.clj$" (. migrate-file getName))) 
        (. migrate-directory listFiles)))))

(defn 
#^{:doc "Returns all of the migration file names as a collection."}
  all-migration-file-names 
  ([] (all-migration-file-names (find-migrate-directory)))
  ([migrate-directory]
    (if migrate-directory
      (map 
        (fn [migration-file] (. migration-file getName))
        (all-migration-files migrate-directory)))))
    
(defn
#^{:doc "Returns the migration number from the given migration file name."}
  migration-number-from-name [migration-file-name]
  (. Integer parseInt (re-find #"^[0-9]+" migration-file-name)))
  
(defn
#^{:doc "Returns the migration number from the given migration file."}
  migration-number-from-file [migration-file]
  (if migration-file
    (migration-number-from-name (. migration-file getName))))
    
(defn
#^{:doc "Returns all of the migration file names with numbers between low-number and high-number inclusive."}
  migration-files-in-range [low-number high-number]
  (let [migrate-directory (find-migrate-directory)]
    (filter 
      (fn [migration-file] 
        (let [migration-number (migration-number-from-file migration-file)]
          (and (>= migration-number low-number) (<= migration-number high-number)))) 
      (all-migration-files migrate-directory))))

(defn 
#^{:doc "Returns all of the numbers prepended to the migration files."}
  all-migration-numbers
  ([] (all-migration-numbers (find-migrate-directory)))
  ([migrate-directory]
    (if migrate-directory
      (map
        (fn [migration-file-name] (migration-number-from-name migration-file-name)) 
        (all-migration-file-names migrate-directory)))))

(defn
#^{:doc "Returns the maximum number of all migration files."}
  max-migration-number
  ([] (max-migration-number (find-migrate-directory)))
  ([migrate-directory]
    (if migrate-directory
      (let [migration-numbers (all-migration-numbers migrate-directory)]
        (if (> (count migration-numbers) 0) 
          (eval (cons max migration-numbers))
          0)))))

(defn 
#^{:doc "Returns the next number to use for a migration file."}
  find-next-migrate-number
  ([] (find-next-migrate-number (find-migrate-directory))) 
  ([migrate-directory]
    (if migrate-directory
      (+ (max-migration-number migrate-directory) 1))))
  
(defn
#^{:doc "The migration file with the given migration name."}
  find-migration-file 
    ([migration-name] (find-migration-file (find-migrate-directory) migration-name))
    ([migrate-directory migration-name]
      (let [migration-file-name-to-find (str (loading-utils/dashes-to-underscores migration-name) ".clj")]
        (seq-utils/find-first 
          (fn [migration-file] 
            (re-find 
              (re-pattern (str "[0-9]+_" migration-file-name-to-find))
              (. migration-file getName)))
          (all-migration-files migrate-directory)))))
          
 (defn
#^{:doc "Returns the migration namespace for the given migration file."}
  migration-namespace [migration-file]
  (if migration-file
    (loading-utils/namespace-string-for-file "migrate" (. migration-file getName))))
  
(defn
#^{:doc "Finds the number of the migration file before the given number"}
  migration-number-before 
    ([migration-number] 
      (if migration-number 
        (migration-number-before migration-number (all-migration-files))))
    ([migration-number migration-files]
      (if migration-number
        (loop [files migration-files
               previous-file-number 0]
          (if (not-empty migration-files)
            (let [migration-file (first files)
                  migration-file-number (migration-number-from-file migration-file)]
              (if (< migration-file-number migration-number)
                (recur (rest files) migration-file-number)
                previous-file-number))
            previous-file-number)))))