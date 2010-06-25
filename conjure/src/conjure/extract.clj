(ns conjure.extract
  (:gen-class)
  (:require [clojure.contrib.command-line :as command-line]
            [clojure.contrib.duck-streams :as duck-streams]
            [clojure.contrib.java-utils :as java-utils] 
            [conjure.core.util.loading-utils :as loading-utils] 
            [conjure.script.new :as conjure-new]))

(def lib-dir-name "lib")
(def default-project-name "*my-project*") 

(defn lib-jar-file-names []
  (filter #(.endsWith % ".jar") (loading-utils/all-class-path-file-names lib-dir-name))) 

(defn copy-libs [project-directory]
  (doseq [jar-file-name (lib-jar-file-names)]
    (let [jar-file-full-path (str lib-dir-name "/" jar-file-name)
          target-jar-file (java-utils/file project-directory jar-file-full-path)]
      (.mkdirs (.getParentFile target-jar-file))
      (duck-streams/copy
        (loading-utils/find-resource jar-file-full-path)
        target-jar-file))))

(defn -main [& args]
  (command-line/with-command-line args
    "java -jar conjure.jar [options] <project name>"
    [[database "What database to use. Currenty valid options are mysql and h2" nil]
     [version? "Prints the current version."] 
     remaining]

    (let [passed-in-project-name (first remaining)
          project-name (loading-utils/underscores-to-dashes passed-in-project-name)
          project-directory (loading-utils/dashes-to-underscores passed-in-project-name)]
      (conjure-new/create-new-project database version? project-directory)
      (copy-libs project-directory)
      (conjure-new/replace-in-file (java-utils/file project-directory "project.clj") default-project-name project-name))))