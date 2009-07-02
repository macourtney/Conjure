(ns conjure.server.server
  (:require [http-config :as http-config]
            [environment :as environment]
            [routes :as routes]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.model.database :as database]
            [conjure.controller.util :as controller-util]
            [conjure.view.util :as view-util]
            [clojure.contrib.str-utils :as str-utils]))

(defn
#^{:doc "Parses the parameters in the given query-string into a parameter map."}
  parse-query-params [query-string]
  (if query-string
      (loop [query-tokens (str-utils/re-split #"&" query-string)
             output {}]
        (let [query-token (first query-tokens)]
          (if query-token
            (let [query-key-value (str-utils/re-split #"=" query-token)]
              (recur (rest query-tokens) 
                     (assoc output (first query-key-value) (second query-key-value))))
            output)))
      {}))

(defn
#^{:doc "Gets a route map for use by conjure to call the correct methods."}
  create-request-map [path params]
  (let [routes-vector (routes/draw)]
    (loop [current-routes routes-vector]
      (if (seq current-routes)
        (let [route-fn (first current-routes)
             output (route-fn path)]
          (if output
            (let [output-params (output :params)]
              (if output-params
                  (assoc output :params (merge output-params params))
                  output))
            (recur (rest current-routes))))))))

(defn
#^{:doc "Returns the controller file name generated from the given request map."}
  controller-file-name [request-map]
  (controller-util/controller-file-name-string (:controller request-map)))
  
(defn
#^{:doc "Returns fully qualified action generated from the given request map."}
  fully-qualified-action [request-map]
  (str (controller-util/controller-namespace (:controller request-map)) "/" (:action request-map)))

(defn
#^{:doc "Loads the given controller file."}
  load-controller [controller-filename]
  (loading-utils/load-resource "controllers" controller-filename))

(defn
#^{:doc "Takes the given path and calls the correct controller and action for it."}
  process-request [request-map]
  (when request-map
    (let [controller-file (controller-file-name request-map)]
      (if controller-file
        (do
          (load-controller controller-file)
          ((load-string (fully-qualified-action request-map)) request-map))
        nil))))

(defn
#^{:doc "A function for simplifying the loading of views."}
  render-view [request-map & params]
  (view-util/load-view request-map)
  (apply (read-string (fully-qualified-action request-map)) request-map params))

(defn
#^{:doc "Gets the user configured http properties."}
  http-config []
  (http-config/get-http-config))

(defn
#^{:doc "Gets the user configured database properties."}
  db-config []
  (database/conjure-db))
  
(defn
#^{:doc "Initializes the conjure server."}
  init []
  (environment/init)
  (database/init-sql))