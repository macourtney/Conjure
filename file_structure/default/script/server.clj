(ns server
  (:require [ring.adapter.jetty :as ring-jetty]
            [conjure.server.server :as conjure-server]
            [conjure.server.ring-adapter :as ring-adapter]))

(ring-jetty/run-jetty ring-adapter/conjure (conjure-server/http-config))