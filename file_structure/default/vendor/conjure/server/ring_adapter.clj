(ns conjure.server.ring-adapter
  (:import [java.io File])
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
#^{ :doc "If reload-files is set, then this function reloads all of the controllers, views, models and helpers." }
  reload []
  (if environment/reload-files
    (apply require :reload 
      (concat 
        (controller-util/all-controller-namespaces) 
        (view-util/all-view-namespaces) 
        (model-util/all-model-namespaces)
        (helper-util/all-helper-namespaces)))))

(defn
#^{ :doc "The ring function which actually calls the conjure server and returns a properly formatted 
request map." }
  call-server [req]
  (try
    (reload)
		(server/process-request { :request req })
    (catch Throwable throwable
      (do
        (logging/error "An error occurred while processing the request." throwable)
        (throw throwable)))))

(defn
#^{ :doc "A Ring adapter function for Conjure." }
  conjure [req]
  ((ring-file/wrap-file (ring-stacktrace/wrap-stacktrace call-server) (new File environment/assets-dir)) req))