(ns helpers.template-helper
  (:require [clj-record.core :as clj-record-core]
            [conjure.model.util :as model-util]
            [conjure.util.request :as request]
            [clojure.tools.string-utils :as string-utils]
            [drift-db.core :as drift-db]))

(defn
#^{ :doc "Returns the metadata for the given model." }
  table-metadata [model-name]
  (model-util/load-model model-name)
  (drift-db/describe-table (clj-record-core/table-name model-name)))

(defn
#^{ :doc "Returns the result of a find records call on the given model with the given attributes" }
  find-records [model-name attributes]
  (model-util/load-model model-name)
  (clj-record-core/find-records model-name attributes))

(defn
#^{ :doc "Returns all of the records of the model with the given name." }
  all-records [model-name]
  (find-records model-name [true]))

(defn
#^{ :doc "Returns the record from the given model with the given id." }
  get-record [model-name id]
  (model-util/load-model model-name)
  (clj-record-core/get-record model-name id))

(defn
#^{ :doc "Gets or generates all of the tab maps for use by template-tabs." }
  all-tabs []
  (or (:tabs (request/layout-info)) []))

(defn-
  template-tabs [request-map]
  (map 
    (fn [tab-map]
      (let [tab-controller (string-utils/str-keyword (:service (:url-for tab-map)))]
        (if (and tab-controller (= tab-controller (:service request-map)))
          (assoc tab-map :is-active true)
          tab-map)))
    (request/set-request-map request-map
      (all-tabs))))

(defn
#^{ :doc "Creates a request-map from the given request map which points to the templates controller with the given 
action or the action in the given request-map" }
  template-request-map 
  ([request-map] (template-request-map request-map (:action request-map)))
  ([request-map action]
    (let [controller (:service request-map)
          tabs (template-tabs request-map)]
      (merge request-map 
        { :service "templates", 
          :action action,
          :layout-info { :tabs (template-tabs request-map) } }))))

(defmacro
  with-template-request-map [& body]
  `(request/with-request-map-fn template-request-map ~@body))

(defmacro
  with-template-action-request-map [action & body]
  `(request/with-request-map-fn 
     (fn [request-map#] (template-request-map request-map# ~action))
     ~@body))