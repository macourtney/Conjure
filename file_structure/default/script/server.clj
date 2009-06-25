(ns server
  (:require [ring.jetty :as ring-jetty]
            [conjure.server.server :as conjure-server]
            [conjure.server.ring_adapter :as ring-adapter]))

(conjure-server/config-server)
(ring-jetty/run (conjure-server/http-config) ring-adapter/conjure)