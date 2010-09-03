(ns leiningen.hooks.copy-resource-deps
  (:use robert.hooke)
  (:require [clojure.contrib.logging :as logging] 
            [clojure.java.io :as io]))

(defn jar-file? [file]
  (and (not (.isDirectory file)) (.endsWith (.getName file) ".jar"))) 

(defn delete-old-resource-files [resource-library-path]
  (let [resource-library-files (filter jar-file? (file-seq resource-library-path))]
    (println "Deleting" (count resource-library-files) "files from" (.getPath resource-library-path)) 
    (doseq [resource-library-file resource-library-files]
      (when-not (.delete resource-library-file)
        (throw (RuntimeException. (str "Could not delete resource library file: " resource-library-file))))))) 

(defn copy-new-resource-files [lein-project resource-library-path]
  (let [jar-files (filter jar-file? (file-seq (io/file (:library-path lein-project))))]
    (println "Copying" (count jar-files) "files to" (.getPath resource-library-path)) 
    (doseq [jar-file jar-files]
      (io/copy jar-file (io/file resource-library-path (.getName jar-file)))))) 

(defn copy-resources [task & args]
  (apply task args)
  (let [lein-project (first args)
        resource-library-path (io/file (str (:root lein-project) "/resources/lib"))]
    (delete-old-resource-files resource-library-path) 
    (copy-new-resource-files lein-project resource-library-path)))

(add-hook #'leiningen.deps/deps copy-resources)