(ns conjure.server.server
  (:require [http-config :as http-config]
            [db-config :as db-config]
            [routes :as routes]
            [conjure.server.jdbc-connector :as jdbc-connector]
            [conjure.util.loading-utils :as loading-utils]
            [conjure.controller.controller :as controller]
            [conjure.view.view :as view]
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
  (controller/controler-file-name-string (:controller request-map)))
  
(defn
#^{:doc "Returns fully qualified action generated from the given request map."}
  fully-qualified-action [request-map]
  (str "controllers." (:controller request-map) "-controller/" (:action request-map)))

(defn
#^{:doc "Loads the given controller file."}
  load-controller [controller-filename]
  (loading-utils/load-resource "controllers" controller-filename))

(defn
#^{:doc "Takes the given path and calls the correct controller and action for it."}
  process-request [request-map]
  (when request-map
    (load-controller (controller-file-name request-map))
    ((load-string (fully-qualified-action request-map)) request-map)))

(defn
#^{:doc "A function for simplifying the loading of views."}
  render-view [request-map & params]
  (view/load-view request-map)
  (apply (read-string (fully-qualified-action request-map)) request-map params))

(defn
#^{:doc "Gets the user configured http properties."}
  http-config []
  (http-config/get-http-config))

(defn db-config []
  (db-config/get-db-config))

(defn
#^{:doc "This is the first method called when the server is started."}
  config-server []
  (http-config)
  (jdbc-connector/init))