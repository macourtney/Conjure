(ns conjure.script.server
  (:require [clojure.tools.cli :as cli]
            [conjure.core.server.server :as conjure-server]
            [conjure.core.server.ring-adapter :as ring-adapter]
            [ring.adapter.jetty :as ring-jetty]))

(defn
  start-server [mode]
  (conjure-server/set-mode mode)
  (conjure-server/init)
  (ring-jetty/run-jetty ring-adapter/conjure (conjure-server/http-config)))

(defn parse-arguments [args]
  (cli/cli args
    ["-m" "--mode" "The server mode. For example, development, production, or test." :default nil]))

(defn
  run [args]
  (let [[args-map remaining help] (parse-arguments args)]
    (start-server (get args-map :mode))))