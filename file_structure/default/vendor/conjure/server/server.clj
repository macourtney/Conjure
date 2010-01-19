(ns conjure.server.server
  (:import [java.util Date])
  (:require [environment :as environment]
            [http-config :as http-config]
            [routes :as routes]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.model.database :as database]
            [conjure.controller.util :as controller-util]
            [conjure.view.util :as view-util]
            [conjure.util.html-utils :as html-utils]
            [conjure.util.session-utils :as session-utils]
            [clojure.contrib.str-utils :as str-utils]
            [clojure.contrib.logging :as logging]))

(def initialized? (ref false))

(defn
#^{:doc "Merges the params value of the given request-map with params"}
  augment-params [request-map params]
  (if request-map
    (let [output-params (request-map :params)]
      (if (and output-params params (not-empty params))
        (assoc request-map :params (merge output-params params))
        request-map))))

(defn
#^{:doc "Returns a parameter map generated from the post content."}
  parse-post-params [request-map]
  (if (= (:request-method request-map) :post)
    (html-utils/parse-query-params 
      (loading-utils/string-input-stream (:body request-map) (:content-length request-map)))
    {}))

(defn
#^{:doc "Parses all of the params from the given request map."}
  parse-params [request-map]
  (merge (parse-post-params request-map) (html-utils/parse-query-params (:query-string request-map))))

(defn
#^{:doc "Gets a route map for use by conjure to call the correct methods."}
  update-request-map [request-map]
  (session-utils/update-request-session 
    (merge request-map 
      (augment-params 
        (or (some identity (map #(% (:uri request-map)) (routes/draw))) { :params {} }) 
        (parse-params request-map)))))

(defn
#^{:doc "Returns the controller file name generated from the given request map."}
  controller-file-name [request-map]
  (controller-util/controller-file-name-string (:controller request-map)))
  
(defn
#^{:doc "Returns fully qualified action generated from the given request map."}
  fully-qualified-action [request-map]
  (if request-map
    (let [controller (:controller request-map)
          action (:action request-map)]
      (if (and controller action)
        (str (controller-util/controller-namespace controller) "/" action)))))

(defn
#^{:doc "Loads the given controller file."}
  load-controller [controller-filename]
  (loading-utils/load-resource "controllers" controller-filename))

(defn
#^{ :doc "Initializes the conjure server." }
  init []
  (if (not @initialized?)
    (do
      (dosync
        (ref-set initialized? true))
      (logging/info "Initializing server...")
      (database/ensure-conjure-db)
      ((:init environment/session-store))
      (logging/info "Server Initialized."))))

(defn 
#^{ :doc "Manages the session cookie in the response map." }
  manage-session [request-map response-map]
  (if (and environment/use-session-cookie)
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
  (let [controller-file (controller-file-name request-map)]
    (when controller-file
      (let [action (fully-qualified-action request-map)]
        (logging/debug (str "Running action: " action))
        (load-controller controller-file)
        (create-response-map 
          ((load-string action) request-map)
          request-map)))))

(defn
#^{ :doc "Initializes the server if necessary and runs the controller based on the given request-map and returns a 
response map.. If the request-map is nil, this function does nothing and returns nil." }
  respond-to [request-map]
  (when request-map
    (init)
    (logging/debug (str "Requested uri: " (:uri request-map)))
    (call-controller (update-request-map request-map))))

(defn
#^{ :doc "Takes the given path and calls the correct controller and action for it." }
  process-request [request-map]
  (let [start-time (new Date)
        response (respond-to request-map)]
    (logging/debug (str "Response time: " (- (.getTime (new Date)) (.getTime start-time)) " ms"))
    response))

(defn
#^{ :doc "A function for simplifying the loading of views." }
  render-view [request-map & params]
  (view-util/load-view request-map)
  (apply (read-string (fully-qualified-action request-map)) request-map params))

(defn
#^{ :doc "Gets the user configured http properties." }
  http-config []
  (http-config/get-http-config))

(defn
#^{:doc "Gets the user configured database properties."}
  db-config []
  database/conjure-db)