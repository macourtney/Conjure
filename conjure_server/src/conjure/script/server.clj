(ns conjure.script.server
  (:require [conjure.server.server :as conjure-server]
            [conjure.server.ring-adapter :as ring-adapter]
            [ring.adapter.jetty :as ring-jetty]))

(defn
  run [args]
  (conjure-server/init-args args)
  (ring-jetty/run-jetty ring-adapter/conjure (conjure-server/http-config)))