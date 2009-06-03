(ns generators.migration-generator
  (:import [java.io File])
  (:use [conjure.util.loading-utils :as loading-utils]
        [conjure.util.string-utils :as string-utils]
        [clojure.contrib.str-utils :as clojure-str-utils]))

(defn
#^{:doc "Prints out how to use the generate migration command."}
  migration-usage []
  (println "You must supply a migration name (Like migration-name).")
  (println "Usage: ./run.sh script/generate.clj migration <migration name>"))
  
(defn 
#^{:doc "Finds the db directory which contains all of the files for updating the schema for the database."}
  find-db-directory []
  (let [db-directory (loading-utils/get-classpath-dir-ending-with "db")]
    (if db-directory
      (do
        (println "Found db directory: " (. db-directory getPath))
        db-directory) 
      (do
        (println "Could not find db directory on path")
        nil))))
        
(defn 
#^{:doc "Finds or creates if missing, the migrate directory in the given db directory."}
  find-or-create-migrate-directory [db-directory]
  (let [migrate-directory (new File (. db-directory getPath) "migrate")]
    (if (. migrate-directory exists)
      (do
        (println "Migrate directory already exists.")
        migrate-directory)
      (do
        (println "Creating migrate directory...")
        (. migrate-directory mkdirs)
        migrate-directory))))
        
(defn 
#^{:doc "Returns all of the migration files as a collection."}
  all-migration-files [migrate-directory]
  (. migrate-directory listFiles))
  
(defn 
#^{:doc "Returns all of the file names as a collection."}
  all-migration-file-names [migrate-directory]
  (map 
    (fn [migration-file] (. migration-file getName))
    (all-migration-files migrate-directory)))
  
(defn 
#^{:doc "Returns all of the numbers prepended to the migration files."}
  all-migration-numbers [migrate-directory]
  (map 
    (fn [migration-file-name] (. Integer parseInt (re-find #"^[0-9]+" migration-file-name))) 
    (all-migration-file-names migrate-directory)))
        
(defn 
#^{:doc "Returns the next number to use for a migration file."}
  find-next-migrate-number [migrate-directory]
  (let [migration-numbers (all-migration-numbers migrate-directory)]
    (if (> (count migration-numbers) 0) 
      (+ (eval (cons max migration-numbers)) 1 )
      1)))
      
(defn
  #^{:doc "Creates a new migration file from the given params."}
  create-migration-file [migrate-directory params]
  (let [next-migrate-number (find-next-migrate-number migrate-directory)
        migration-file-name (str (string-utils/prefill (str next-migrate-number) 3 "0") "_" (clojure-str-utils/re-gsub #"-" "_" (first params)) ".clj")
        migration-file (new File migrate-directory  migration-file-name)]
    (println "Creating migration file" migration-file-name "...")
    (. migration-file createNewFile)))

(defn 
#^{:doc "Generates a migration file for the migration name given in params."}
  generate-migration [params]
  (let [migration-name (first params)]
    (if migration-name
      (let [db-directory (find-db-directory)]
        (if db-directory
          (let [migrate-directory (find-or-create-migrate-directory db-directory)] 
            (create-migration-file migrate-directory params))))
      (migration-usage))))