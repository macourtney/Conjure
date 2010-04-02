(ns conjure.server.server
  (:import [java.util Date])
  (:require [clojure.contrib.java-utils :as java-utils]
            [clojure.contrib.logging :as logging]
            [clojure.contrib.str-utils :as str-utils]
            [conjure.controller.util :as controller-util]
            [conjure.model.database :as database]
            [conjure.util.html-utils :as html-utils]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.util.session-utils :as session-utils]
            [conjure.util.string-utils :as conjure-str-utils]
            [conjure.view.util :as view-util]
            environment
            http-config
            routes
            session-config))

(def initialized? (ref false))

(defn
#^{ :doc "Merges the params value of the given request-map with params" }
  augment-params [request-map params]
  (if request-map
    (let [output-params (request-map :params)]
      (if (and output-params params (not-empty params))
        (assoc request-map :params (merge output-params params))
        request-map))))

(defn
#^{ :doc "Returns a parameter map generated from the post content." }
  parse-post-params [request-map]
  (let [request (:request request-map)
        content-type (:content-type request)]
    (if 
      (and 
        (= (:request-method request) :post)
        content-type
        (.startsWith content-type "application/x-www-form-urlencoded"))
  
      (html-utils/parse-query-params 
        (loading-utils/string-input-stream (:body request) (:content-length request)))
      {})))

(defn
#^{ :doc "Parses all of the params from the given request map." }
  parse-params [request-map]
  (merge (parse-post-params request-map) (html-utils/parse-query-params (:query-string (:request request-map)))))

(defn
#^{ :doc "Gets a route map for use by conjure to call the correct methods." }
  update-request-map [request-map]
  (session-utils/update-request-session 
    (merge request-map 
      (augment-params 
        (or (some identity (map #(% (:uri (:request request-map))) (routes/draw))) { :params {} }) 
        (parse-params request-map)))))

(defn
#^{ :doc "Initializes the conjure server." }
  init []
  (if (not @initialized?)
    (do
      (dosync
        (ref-set initialized? true))
      (environment/init)
      (logging/info "Initializing server...")
      (database/ensure-conjure-db)
      ((:init session-config/session-store))
      (logging/info "Server Initialized."))))

(defn 
#^{ :doc "Manages the session cookie in the response map." }
  manage-session [request-map response-map]
  (if (and session-config/use-session-cookie)
    (session-utils/manage-session request-map response-map)
    response-map))

(defn
#^{ :doc "Converts the given response to a response map if it is not already 
one." }
  create-response-map [response request-map]
  (manage-session 
    request-map
    (if (map? response)
      response
      {:status  200
       :headers {"Content-Type" "text/html"}
       :body    response})))
     
(defn
#^{ :doc "Calls the given controller with the given request map returning the response." }
  call-controller [request-map]
  (let [response (controller-util/call-controller request-map)]
    (if response
      (create-response-map response request-map)
      (controller-util/call-controller { :controller "home", :action "error-404" }))))

(defn
#^{ :doc "Takes the given path and calls the correct controller and action for it." }
  process-request [request-map]
  (when request-map
    (init)
    (logging/debug (str "Requested uri: " (:uri (:request request-map))))
    (call-controller (update-request-map request-map))))

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
  (if mode 
    (java-utils/set-system-properties 
      { environment/conjure-environment-property (conjure-str-utils/str-keyword mode) })))