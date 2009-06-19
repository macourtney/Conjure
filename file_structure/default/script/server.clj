(ns server
  (:require [ring.jetty :as ring_jetty]
            [conjure.server.server :as conjure_server]
            [conjure.server.ring_adapter :as ring_adapter]))

(ring_jetty/run (conjure_server/http-config) ring_adapter/conjure)