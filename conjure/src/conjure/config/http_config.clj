(ns conjure.config.http-config
  (:require [conjure.util.loading-utils :as loading-utils]))

(defn default-get-http-config []
  ;; server port
  {:port 8080

   ;; static pages will be served from this directory
   :static-dir "./public"

   ;; log files go here
   :log-dir "./log"

   ;; specifies naming pattern for log files
   :log-pattern "log.yyyy_mm_dd.txt"})

(defn
#^{ :doc "Returns the value of the given var symbol in the http config namespace or default if the var or the namespace
cannot be found.." }
  resolve-http-config-var [var-sym default]
  (loading-utils/resolve-ns-var 'http-config var-sym default))

(defn
  get-http-config []
  ((resolve-http-config-var 'get-http-config default-get-http-config)))