(ns bindings.generic.delete-warning
  (:use conjure.binding.base)
  (:require [conjure.model.util :as model-util]))

(defbinding [request-map model-name]
  (let [id (:id (:params request-map))]
    (let [model-namespace (find-ns (symbol (model-util/model-namespace model-name)))]
      (render-view (merge request-map { :controller "templates" :action "delete-warning" })
        model-name
        ((ns-resolve  model-namespace 'table-metadata))
        (if id ((ns-resolve  model-namespace 'get-record) id))))))