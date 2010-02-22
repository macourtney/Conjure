(ns server
  (:require [clojure.contrib.command-line :as command-line]
            [conjure.server.server :as conjure-server]
            [conjure.server.ring-adapter :as ring-adapter]
            [environment]
            [ring.adapter.jetty :as ring-jetty]))

(command-line/with-command-line *command-line-args*
  "./run.sh script/server.clj [options]"
  [[mode "The server mode. For example, development, production, or test." nil]
   remaining]

  (conjure-server/set-mode mode)
  (conjure-server/init)
  (ring-jetty/run-jetty ring-adapter/conjure (conjure-server/http-config)))

