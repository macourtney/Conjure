(ns conjure.binding.base
  (:require [conjure.binding.util :as binding-util]
            [conjure.view.util :as view-util]))

(defmacro def-binding [params & body]
  (let [controller-action-map (binding-util/controller-action-map (name (ns-name *ns*)))]
    `(binding-util/add-bind-function
      (fn ~params ~@body)
      ~controller-action-map)))

(defn render-view [& params]
  (apply view-util/render-view params))