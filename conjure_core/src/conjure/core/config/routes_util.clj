(ns conjure.core.config.routes-util
  (:require [clojure.contrib.logging :as logging]
            [clout.core :as clout]
            [config.routes :as routes]
            [conjure.core.controller.util :as controller-util]
            [conjure.core.server.request :as request]
            [conjure.core.util.loading-utils :as loading-utils]))

(defn
  function-parse []
  (let [route-functions (:functions routes/routes)]
    (when (and route-functions (not-empty route-functions))
      (some identity (map #(%) route-functions)))))

(defn
  symbol-replace [request-map symbol-map]
  (reduce
    (fn [output-map [request-key request-value]]
      (cond
        (symbol? request-value)
          (assoc output-map request-key (get symbol-map (str request-value)))
        (map? request-value)
          (assoc output-map request-key (symbol-replace request-value symbol-map))
        true
          (assoc output-map request-key request-value)))
    {}
    request-map))

(defn
  clean-controller-action [request-map]
  (reduce
    (fn [output [request-key request-value]]
      (cond
        (or (= request-key :controller) (= request-key :action))
          (assoc output request-key (loading-utils/underscores-to-dashes request-value))
        true
          (assoc output request-key request-value)))
    {}
    request-map))

(defn
  parse-compiled-route [compiled-route-map]
  (let [route (:route compiled-route-map)
        request-map (:request-map compiled-route-map)]
    (when (and route request-map)
      (let [route-map (clout/route-matches route (request/uri))]
        (when route-map
          (clean-controller-action (symbol-replace request-map route-map)))))))

(defn
  compiled-parse []
  (let [compiled-routes (:compiled routes/routes)]
    (when compiled-routes
      (some identity (map parse-compiled-route compiled-routes)))))

(defn
  parse-path []
  (or (function-parse) (compiled-parse)))

(defn
#^{ :doc "Calls the controller action specified in the given path-map. Path-map must contain :controller and :action 
keys, and may contain optional :id key. The controller, action and id will be appropriately merged into the request-map
before calling the controller action." }
  call-controller [path-map]
  (request/with-merged-request-map path-map
    (controller-util/call-controller)))

(defn
#^{ :doc "This function calls the appropriate controller and action." }
  route-request []
  (let [path-map (parse-path)]
    (when path-map
      (call-controller path-map))))