(ns conjure.core.server.ring-adapter
  (:import [java.io File FileInputStream]
           [java.util Date])
  (:require [clojure.contrib.classpath :as classpath]
            [clojure.contrib.logging :as logging]
            [conjure.core.config.environment :as environment]
            [conjure.core.controller.util :as controller-util]
            [conjure.core.helper.util :as helper-util]
            [conjure.core.model.util :as model-util]
            [conjure.core.server.server :as server]
            [conjure.core.util.file-utils :as file-utils]
            [conjure.core.util.loading-utils :as loading-utils]
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
  find-resource [request full-path]
  (if-let [body (loading-utils/find-resource full-path)]
    body
    (if-let [servlet-context (:servlet-context request)]
      (when-let [resource-file-path (.getRealPath servlet-context (str "WEB-INF/classes/" full-path))]
        (let [resource-file (File. resource-file-path)]
          (when (.exists resource-file)
            (FileInputStream. resource-file))))
      (when-let [resource-file (File. (file-utils/user-directory) full-path)]
        (when (.exists resource-file)
          (FileInputStream. resource-file))))))

(defn
  wrap-resource-dir [app root-path]
  (fn [request]
    (if (= :get (:request-method request))
      (let [resource-path (codec/url-decode (:uri request))]
        (if (.endsWith resource-path "/")
          (app request)
          (if-let [body (find-resource request (str root-path resource-path))]
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