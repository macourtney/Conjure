(ns conjure.config.routes-util
  (:require [clout.core :as clout]
            [config.routes :as routes]
            [conjure.config.environment :as environment]
            [conjure.util.request :as request]
            [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.logging :as logging]
            [clojure.tools.servlet-utils :as servlet-utils]))

(defn
  function-parse []
  (let [route-functions (:functions routes/routes)]
    (when (and route-functions (not-empty route-functions))
      (some identity (map #(%) route-functions)))))

(defn get-symbol-value [symbol-map request-value]
  (let [request-value-str (str request-value)]
    (or (get symbol-map request-value-str) (get symbol-map (keyword request-value-str)))))

(defn
  symbol-replace [request-map symbol-map]
  (reduce
    (fn [output-map [request-key request-value]]
      (cond
        (symbol? request-value)
          (assoc output-map request-key (get-symbol-value symbol-map request-value))
        (map? request-value)
          (assoc output-map request-key (symbol-replace request-value symbol-map))
        true
          (assoc output-map request-key request-value)))
    {}
    request-map))

(defn
  clean-service-action [request-map]
  (reduce
    (fn [output [request-key request-value]]
      (assoc output request-key 
             (if (or (= request-key :controller) (= request-key :service) (= request-key :action))
               (loading-utils/underscores-to-dashes request-value)
               request-value)))
    {}
    request-map))

(defn
  parse-compiled-route [compiled-route-map ring-request]
  (when ring-request
    (when-let [route (:route compiled-route-map)]
      (when-let [request-map (:request-map compiled-route-map)]
        (when-let [route-map (clout/route-matches route ring-request)]
          (clean-service-action (symbol-replace request-map route-map)))))))

(defn
  compiled-parse
  ([] (compiled-parse (request/ring-request)))
  ([ring-request]
    (when-let [compiled-routes (:compiled routes/routes)]
      (some identity (map #(parse-compiled-route % ring-request) compiled-routes)))))

(defn
  servlet-parse []
  (when-let [servlet-context (request/servlet-context)]
    (let [uri (request/uri)]
      (when (servlet-utils/servlet-uri? servlet-context uri)
        (compiled-parse (assoc (request/ring-request) :uri (servlet-utils/servlet-sub-path servlet-context uri)))))))

(defn
  parse-path []
  (or (function-parse) (compiled-parse) (servlet-parse)))

(defn
#^{ :doc "Calls the service action specified in the given path-map. Path-map must contain :service and :action 
keys, and may contain optional :id key. The service, action and id will be appropriately merged into the request-map
before calling the service action." }
  call-service [path-map]
  (request/with-merged-request-map path-map
    (logging/debug (str "Requested service: " (request/service) ", action: " (request/action)))
    (environment/call-service)))

(defn
#^{ :doc "This function calls the appropriate service and action." }
  route-request []
  (when-let [path-map (parse-path)]
    (call-service path-map)))