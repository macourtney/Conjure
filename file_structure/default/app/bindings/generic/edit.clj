(ns bindings.generic.edit
  (:use conjure.binding.base)
  (:require [conjure.model.util :as model-util]))

(defbinding [request-map model-name]
  (let [id (:id (:params request-map))]
    (let [model-namespace (find-ns (symbol (model-util/model-namespace model-name)))]
      (render-view (merge request-map { :controller "templates" :action "edit" })
        model-name
        ((ns-resolve  model-namespace 'table-metadata))
        ((ns-resolve  model-namespace 'get-record) id)))))