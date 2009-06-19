(ns conjure.server.ring_adapter
  (:require [conjure.server.server :as server]))

(defn
#^{:doc "A Ring adapter function for Conjure."}
  conjure [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (server/process-request (:uri req) (server/parse-query-params (:query-string req)))})