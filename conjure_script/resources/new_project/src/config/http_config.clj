;; This file is used to configure the http server.

(ns config.http-config)

(defn get-http-config []
  ;; server port
  {:port 8080

   ;; static pages will be served from this directory
   :static-dir "./public"

   ;; log files go here
   :log-dir "./log"

   ;; specifies naming pattern for log files
   :log-pattern "log.yyyy_mm_dd.txt"})