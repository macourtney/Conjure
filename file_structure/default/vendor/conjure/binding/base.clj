(ns conjure.binding.base
  (:require [conjure.binding.util :as binding-util]
            [conjure.view.util :as view-util]))

(defmacro defbinding [params & body]
  (let [controller-action-map (binding-util/controller-action-map (name (ns-name *ns*)))]
    `(binding-util/add-bind-function
      (fn ~params ~@body)
      ~controller-action-map)))

(defn
#^{ :doc "Determines the type of render-view called. Possible values: :request-map, :parameters." }
  render-type? [request-map & params]
  (if (and (contains? request-map :controller) (contains? request-map :action))
    :request-map
    :parameters))

(defmulti render-view "Renders the view given in the request-map." render-type?)

(defmethod render-view :request-map [request-map & params]
  (apply render-view { :layout "application" } request-map params))

(defmethod render-view :parameters [parameters request-map & params]
  (view-util/render-layout (:layout parameters) request-map
    (apply view-util/render-view request-map params)))