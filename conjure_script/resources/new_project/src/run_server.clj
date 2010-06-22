(ns run-server
  (:require [conjure.script.server :as server]) 
  (:gen-class))

(defn -main [& args]
  (command-line/with-command-line args
    "java -jar <jar name> [options]"
    [[mode "The server mode. For example, development, production, or test." "production"]
     remaining]
    (server/start-server mode)))