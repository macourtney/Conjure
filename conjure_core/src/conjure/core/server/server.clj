(ns conjure.core.server.server
  (:import [java.util Date])
  (:require [clojure.contrib.java-utils :as java-utils]
            [clojure.contrib.logging :as logging]
            [config.http-config :as http-config]
            [conjure.core.config.routes-util :as routes-util]
            [config.session-config :as session-config]
            [conjure.core.config.environment :as environment]
            [conjure.core.controller.util :as controller-util]
            [conjure.core.model.database :as database]
            [conjure.core.plugin.util :as plugin-util]
            [conjure.core.server.request :as request]
            [conjure.core.util.session-utils :as session-utils]
            [clojure.tools.string-utils :as conjure-str-utils]))

(def initialized? (atom false))

(def init? (promise))

(defn
  init-promise-fn []
  (environment/require-environment)
  (database/init-database)
  ((:init session-config/session-store))
  (logging/info "Server Initialized.")
  (logging/info "Initializing plugins...")
  (plugin-util/initialize-all-plugins)
  (logging/info "Plugins initialized.")
  (logging/info "Initializing app controller...")
  (try
    (require 'controllers.app)
    (logging/info "App controller initialized.")
    (deliver init? true)
  (catch Throwable t
    (logging/error "Failed to initialize app controller." t)
    (deliver init? false))))

(defn
#^{ :doc "Initializes the conjure server." }
  init []
  (when (compare-and-set! initialized? false true)
    (init-promise-fn))
  @init?)

(defn 
#^{ :doc "Manages the session cookie in the response map." }
  manage-session [response-map]
  (if session-config/use-session-cookie
    (session-utils/manage-session response-map)
    response-map))

(defn
#^{ :doc "Converts the given response to a response map if it is not already 
one." }
  create-response-map [response]
  (manage-session
    (if (map? response)
      response
      { :status  200
        :headers { "Content-Type" "text/html" }
        :body    response })))
     
(defn
#^{ :doc "Calls the given controller with the given request map returning the response." }
  call-controller []
  (let [response (routes-util/route-request)]
    (if response
      (create-response-map response)
      (request/set-request-map { :controller "home", :action "error-404" }
        (controller-util/call-controller)))))

(defn
#^{ :doc "Takes the given path and calls the correct controller and action for it." }
  process-request [request-map]
  (when request-map
    (init)
    (request/with-updated-request-map request-map
      (logging/debug (str "Requested uri: " (request/uri)))
      (session-utils/with-request-session
        (call-controller)))))

(defn
#^{ :doc "Gets the user configured http properties." }
  http-config []
  (http-config/get-http-config))

(defn
#^{ :doc "Gets the user configured database properties." }
  db-config []
  database/conjure-db)

(defn
#^{ :doc "Sets the server mode to the given mode. The given mode must be a keyword or string like development, 
production, or test." }
  set-mode [mode]
  (when mode 
    (environment/set-evironment-property (conjure-str-utils/str-keyword mode))))