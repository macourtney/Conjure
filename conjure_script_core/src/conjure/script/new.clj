(ns conjure.script.new
  (:import [java.io BufferedInputStream File])
  (:require [clojure.tools.cli :as cli]
            [clojure.java.io :as duck-streams]
            [clojure.tools.file-utils :as file-utils]
            [clojure.tools.loading-utils :as loading-utils]))

(def conjure-version "0.8.0-SNAPSHOT") 
(def default-database "h2")
(def default-session-store "database-session-store")
(def default-use-logger? ":use-logger? true") 

(def google-app-engine-session-store "google-app-engine-session-store")
(def google-app-engine-use-logger? ":use-logger? false") 

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
  (when (not (.exists entry-file))
    (.mkdirs (.getParentFile entry-file)) 
    (with-open [fileInputStream (BufferedInputStream. (ClassLoader/getSystemResourceAsStream (.getName zip-entry)))]
      (duck-streams/copy fileInputStream entry-file))))

(defn
  extract-zip-entry 
  ([directory zip-entry] (extract-zip-entry directory zip-entry new-project-zip-directory))
  ([directory zip-entry zip-directory-name]
    (let [entry-file (entry-file directory zip-entry zip-directory-name)]
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
  replace-in-file [file target replacement]
  (spit
    file 
    (.replace (slurp file) target replacement)))

(defn
  set-database 
  ([database] (set-database database (file-utils/user-directory)))
  ([database directory]
    (replace-in-file (File. directory "/src/config/db_config.clj") default-database database)
    (when (= database "google-app-engine")
      (replace-in-file (File. directory "/src/config/environment.clj")
        default-use-logger?
        google-app-engine-use-logger?)
      (replace-in-file (File. directory "/src/config/session_config.clj")
        default-session-store
        google-app-engine-session-store))))

(defn print-version []
  (println "Conjure Version:" conjure-version))

(defn
  create-new-project 
  ([database version?] (create-new-project database version? (file-utils/user-directory)))
  ([database version? directory]
    (if version? 
      (print-version) 
      (extract-all directory))
    (when database
      (set-database database directory)))) 

(defn parse-args [args]
  (cli/cli args
    ["-d" "--database" "What database to use. Currenty valid options are mysql, h2, and google-app-engine"]
    ["-v" "--version" "Prints the current version." :flag true]))

(defn
  run [args]
  (let [args-vec (parse-args args)
        args-map (second args-vec)]
  (create-new-project (:database args-map) (:version args-map))))