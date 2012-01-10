(ns conjure.core.config.routes-util
  (:require [clout.core :as clout]
            [config.routes :as routes]
            [conjure.core.config.environment :as environment]
            [conjure.core.util.request :as request]
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
  parse-compiled-route [compiled-route-map ring-request]
  (when ring-request
    (when-let [route (:route compiled-route-map)]
      (when-let [request-map (:request-map compiled-route-map)]
        (when-let [route-map (clout/route-matches route ring-request)]
          (clean-controller-action (symbol-replace request-map route-map)))))))

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
#^{ :doc "Calls the controller action specified in the given path-map. Path-map must contain :controller and :action 
keys, and may contain optional :id key. The controller, action and id will be appropriately merged into the request-map
before calling the controller action." }
  call-controller [path-map]
  (request/with-merged-request-map path-map
    (environment/call-controller)))

(defn
#^{ :doc "This function calls the appropriate controller and action." }
  route-request []
  (when-let [path-map (parse-path)]
    (call-controller path-map)))