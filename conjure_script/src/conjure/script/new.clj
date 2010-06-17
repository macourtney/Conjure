(ns conjure.script.new
  (:import [java.io BufferedInputStream File])
  (:require [clojure.contrib.command-line :as command-line]
            [clojure.contrib.duck-streams :as duck-streams]
            [conjure.core.util.file-utils :as file-utils]
            [conjure.core.util.loading-utils :as loading-utils]))

(def conjure-version "0.7.0-SNAPSHOT") 
(def default-database "h2")

(def new-project-zip-directory "new_project/") 

(defn new-project-zip-entries []
  (loading-utils/class-path-zip-entries "new_project/")) 

(defn
  entry-file
  ([directory zip-entry] (entry-file directory zip-entry new-project-zip-directory))
  ([directory zip-entry zip-directory-name]
    (File. directory (.substring (.getName zip-entry) (count zip-directory-name))))) 

(defn
  create-file [entry-file zip-entry]
  (.mkdirs (.getParentFile entry-file)) 
  (with-open [fileInputStream (BufferedInputStream. (ClassLoader/getSystemResourceAsStream (.getName zip-entry)))]
   (duck-streams/copy fileInputStream entry-file)))

(defn
  extract-zip-entry 
  ([directory zip-entry] (extract-zip-entry directory zip-entry new-project-zip-directory))
  ([directory zip-entry zip-directory-name]
    (let [entry-file (entry-file directory zip-entry zip-directory-name)]
      (println "Creating:" (.getPath entry-file))
      (if (.isDirectory zip-entry)
        (.mkdir entry-file)
        (create-file entry-file zip-entry)))))

(defn extract-all
  ([] (extract-all (file-utils/user-directory)))  
  ([directory] (extract-all directory (new-project-zip-entries)))
  ([directory zip-entries] (extract-all directory zip-entries new-project-zip-directory)) 
  ([directory zip-entries zip-directory-name]
    (doseq [zip-entry zip-entries]
      (extract-zip-entry directory zip-entry zip-directory-name)))) 

(defn
  set-database 
  ([database])
  ([database directory]
    (let [db-config-file (File. directory "/src/config/db_config.clj")]
      (duck-streams/spit
        db-config-file 
        (.replaceAll (duck-streams/slurp* db-config-file) default-database database)))))

(defn print-version []
  (println "Conjure Version:" conjure-version))

(defn
  run [args]
  (command-line/with-command-line args
    "./run.sh script/server.clj [options]"
    [[database "What database to use. Currenty valid options are mysql and h2" nil]
     [version? "Prints the current version."] 
     remaining]
    (if version? 
      (print-version) 
      (extract-all))
    (when database
      (set-database database))))