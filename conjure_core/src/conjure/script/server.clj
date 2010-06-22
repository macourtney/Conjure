(ns conjure.script.server
  (:require [clojure.contrib.command-line :as command-line]
            [conjure.core.server.server :as conjure-server]
            [conjure.core.server.ring-adapter :as ring-adapter]
            [ring.adapter.jetty :as ring-jetty]))

(defn
  start-server [mode]
  (conjure-server/set-mode mode)
  (conjure-server/init)
  (ring-jetty/run-jetty ring-adapter/conjure (conjure-server/http-config)))

(defn
  run [args]
  (command-line/with-command-line args
    "./run.sh script/server.clj [options]"
    [[mode "The server mode. For example, development, production, or test." nil]
     remaining]
  
    (start-server mode)))