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
#^{:doc "Adds the given query-key-value sequence as a key value pair to the map params."}
  add-param [params query-key-value]
  (assoc params (keyword (first query-key-value)) (second query-key-value)))

(defn
#^{:doc "Parses the parameters in the given query-string into a parameter map."}
  parse-query-params [query-string]
  (if query-string
    (reduce add-param {} (filter second (map #(str-utils/re-split #"=" %) (str-utils/re-split #"&" query-string))))
    {}))

(defn
#^{:doc "Merges the params value of the given request-map with params"}
  augment-params [request-map params]
  (if request-map
    (let [output-params (request-map :params)]
      (if (and output-params params (not-empty params))
        (assoc request-map :params (merge output-params params))
        request-map))))

(defn
#^{:doc "Gets a route map for use by conjure to call the correct methods."}
  update-request-map [request-map]
  (let [path (:uri request-map)
        params (parse-query-params (:query-string request-map))
        output (augment-params (some identity (map #(% path) (routes/draw))) params)]
    (if output
      (merge request-map output)
      (assoc request-map :params params )))) 

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
#^{:doc "Takes the given path and calls the correct controller and action for it."}
  process-request [request-map]
  (when request-map
    (let [generated-request-map (update-request-map request-map)
          controller-file (controller-file-name generated-request-map)]
      (if controller-file
        (do
          (load-controller controller-file)
          ((load-string (fully-qualified-action generated-request-map)) generated-request-map))
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
  database/conjure-db)
  
(defn
#^{:doc "Initializes the conjure server."}
  init []
  (environment/init)
  (database/init-sql))