(ns bindings.generic.add
  (:use conjure.binding.base)
  (:require [conjure.model.util :as model-util]))

(defbinding [request-map model-name]
  (let [model-namespace (find-ns (symbol (model-util/model-namespace model-name)))]
    (render-view (merge request-map { :controller "templates" :action "add" })
      model-name
      ((ns-resolve  model-namespace 'table-metadata)))))