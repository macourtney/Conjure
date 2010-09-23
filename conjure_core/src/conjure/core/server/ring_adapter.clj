(ns conjure.core.server.ring-adapter
  (:import [java.io File FileInputStream]
           [java.util Date])
  (:require [clojure.contrib.classpath :as classpath]
            [clojure.contrib.logging :as logging]
            [clojure_util.file-utils :as file-utils]
            [clojure_util.loading-utils :as loading-utils]
            [clojure_util.servlet-utils :as servlet-utils]
            [conjure.core.config.environment :as environment]
            [conjure.core.controller.util :as controller-util]
            [conjure.core.helper.util :as helper-util]
            [conjure.core.model.util :as model-util]
            [conjure.core.server.request :as request]
            [conjure.core.server.server :as server]
            [conjure.core.view.util :as view-util]
            [ring.middleware.file :as ring-file]
            [ring.middleware.stacktrace :as ring-stacktrace]
            [ring.util.codec :as codec]
            [ring.util.response :as response]))

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
  resource-relative-path [request root-path resource-path]
  (str root-path (servlet-utils/convert-servlet-path (request/servlet-context request) resource-path)))

(defn
  wrap-resource-dir [app root-path]
  (fn [request]
    (if (= :get (:request-method request))
      (let [resource-path (codec/url-decode (:uri request))]
        (if (.endsWith resource-path "/")
          (app request)
          (if-let [body (servlet-utils/find-resource (request/servlet-context request) (resource-relative-path request root-path resource-path))]
            (response/response body)
            (app request))))
      (app request))))

(defn
#^{ :doc "A Ring adapter function for Conjure." }
  conjure [req]
  ((wrap-resource-dir 
    (ring-stacktrace/wrap-stacktrace (wrap-response-time call-server)) 
    environment/assets-dir)
    req))