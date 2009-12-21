(ns conjure.server.ring-adapter
  (:import [java.io File])
  (:require [conjure.server.server :as server]
            [environment :as environment]
            [ring.middleware.file :as ring-file]
            [ring.middleware.stacktrace :as ring-stacktrace]))

(defn
#^{:doc "The ring function which actually calls the conjure server and returns a properly formatted 
request map."}
  call-server [req]
  (try
		(server/process-request req)
    (catch Throwable throwable
      (do
        (. throwable printStackTrace)
        (throw throwable)))))

(defn
#^{:doc "A Ring adapter function for Conjure."}
  conjure [req]
  ((ring-file/wrap-file (ring-stacktrace/wrap-stacktrace call-server) (new File environment/assets-dir)) req))