(ns run
  (:require [clojure.contrib.command-line :as command-line]
            [conjure.core.execute :as core-execute]
            [conjure.script.migrate :as migrate] 
            [conjure.script.server :as server]) 
  (:gen-class))

(defn
  run-server [args]
  (command-line/with-command-line args
    "java -jar <your project jar> server [options]"
    [[mode "The server mode. For example, development, production, or test." "production"]
     remaining]

    (server/start-server mode)))

(defn
  run-migrate [args]
  (command-line/with-command-line args
    "java -jar <your project jar> migrate [options]"
    [ [version "The version to migrate to. Example: -version 0 -> migrates to version 2." nil]
      [mode "The server mode. For example, development, production, or test." "production"]
      remaining]
  
    (migrate/run args))) 

(defn -main [& args]
  (let [action (first args)]
    (cond
      (empty? args) (server/start-server "production")
      (= "server" action) (run-server (rest args)) 
      (= "migrate" action) (run-migrate (rest args)) 
      true (println "Unknown action:" action))))