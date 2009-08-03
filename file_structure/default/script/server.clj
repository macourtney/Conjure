(ns server
  (:require [ring.jetty :as ring-jetty]
            [conjure.server.server :as conjure-server]
            [conjure.server.ring-adapter :as ring-adapter]))


(conjure-server/init)
(ring-jetty/run (conjure-server/http-config) ring-adapter/conjure)