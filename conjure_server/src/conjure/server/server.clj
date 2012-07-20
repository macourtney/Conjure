(ns conjure.server.server
  (:import [java.util Date])
  (:require [config.db-config :as db-config]
            [config.http-config :as http-config]
            [config.session-config :as session-config]
            [conjure.config.environment :as environment]
            [conjure.config.routes-util :as routes-util]
            [conjure.plugin.util :as plugin-util]
            [conjure.util.request :as request]
            [conjure.util.session-utils :as session-utils]
            [clojure.tools.cli :as cli]
            [clojure.tools.logging :as logging]
            [clojure.tools.string-utils :as conjure-str-utils]
            [drift-db.core :as drift-db]))

(def initialized? (atom false))

(def init? (promise))

(defn
  init-promise-fn []
  (environment/require-environment)
  (drift-db/init-flavor (db-config/load-config))
  ((:init session-config/session-store))
  (logging/info "Server Initialized.")
  (logging/info "Initializing plugins...")
  (plugin-util/initialize-all-plugins)
  (logging/info "Plugins initialized.")
  (logging/info "Initializing app flow...")
  (try
    (require 'flows.app)
    (logging/info "App flow initialized.")
    (deliver init? true)
  (catch Throwable t
    (logging/error "Failed to initialize app flow." t)
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
#^{ :doc "Calls the given service with the given request map returning the response." }
  call-service []
  (if-let [response (routes-util/route-request)]
    (create-response-map response)
    (request/set-request-map { :service "home", :action "error-404" }
      (environment/call-service))))

(defn
#^{ :doc "Takes the given path and calls the correct service and action for it." }
  process-request [request-map]
  (when request-map
    (init)
    (request/with-updated-request-map request-map
      (logging/debug (str "Requested uri: " (request/uri)))
      (session-utils/with-request-session
        (call-service)))))

(defn
#^{ :doc "Gets the user configured http properties." }
  http-config []
  (http-config/get-http-config))

(defn
#^{ :doc "Sets the server mode to the given mode. The given mode must be a keyword or string like development, 
production, or test." }
  set-mode [mode]
  (when mode 
    (environment/set-evironment-property (conjure-str-utils/str-keyword mode))))

(defn parse-arguments [args]
  (cli/cli args
    ["-m" "--mode" "The server mode. For example, development, production, or test." :default nil]))

(defn init-args [args]
  (let [[args-map remaining help] (parse-arguments args)]
    (set-mode (get args-map :mode))
    (init)))