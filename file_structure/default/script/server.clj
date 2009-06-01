(ns server (:use [conjure.server.jetty-server]))

(def server (make-server))

(. server (start))
