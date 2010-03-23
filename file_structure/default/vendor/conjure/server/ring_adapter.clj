(ns conjure.server.ring-adapter
  (:import [java.io File]
           [java.util Date])
  (:require [clojure.contrib.logging :as logging]
            [conjure.controller.util :as controller-util]
            [conjure.helper.util :as helper-util]
            [conjure.model.util :as model-util]
            [conjure.server.server :as server]
            [conjure.view.util :as view-util]
            [environment :as environment]
            [ring.middleware.file :as ring-file]
            [ring.middleware.stacktrace :as ring-stacktrace]))

(defn
#^{ :doc "The ring function which actually calls the conjure server and returns a properly formatted 
request map." }
  call-server [req]
  (try
		(server/process-request { :request req })
    (catch Throwable throwable
      (do
        (logging/error "An error occurred while processing the request." throwable)
        (throw throwable)))))

(defn
  wrap-response-time [app]
  (fn [req]
    (let [start-time (new Date)
          response (app req)]
      (logging/debug (str "Response time: " (- (.getTime (new Date)) (.getTime start-time)) " ms"))
      response)))

(defn
#^{ :doc "A Ring adapter function for Conjure." }
  conjure [req]
  ((ring-file/wrap-file 
    (ring-stacktrace/wrap-stacktrace (wrap-response-time call-server)) 
    (new File environment/assets-dir)) 
    req))