(ns conjure.server.ring_adapter
  (:import [java.io File])
  (:require [conjure.server.server :as server]
            [ring.file :as ring-file]))

(defn
#^{:doc "The ring function which actually calls the conjure server and returns a properly formatted 
request map."}
  call-server [req]
  (println "conjure ring function called")
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (server/process-request (:uri req) (server/parse-query-params (:query-string req)))})

(defn
#^{:doc "A Ring adapter function for Conjure."}
  conjure [req]
  ((ring-file/wrap (new File "public") call-server) req))