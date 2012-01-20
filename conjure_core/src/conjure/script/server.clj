(ns conjure.script.server
  (:require [clojure.tools.cli :as cli]
            [conjure.core.server.server :as conjure-server]
            [conjure.core.server.ring-adapter :as ring-adapter]
            [ring.adapter.jetty :as ring-jetty]))

(defn
  run [args]
  (conjure-server/init-args args)
  (ring-jetty/run-jetty ring-adapter/conjure (conjure-server/http-config)))