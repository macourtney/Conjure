(ns bindings.generic.ajax-save
  (:use conjure.binding.base)
  (:require [conjure.model.util :as model-util]))

(defbinding [request-map model-name record]
  (if record
    (let [model-namespace (find-ns (symbol (model-util/model-namespace model-name)))]
      (render-view { :layout nil } (merge request-map { :controller "templates" :action "record-row" })
        model-name
        ((ns-resolve  model-namespace 'table-metadata))
        record))))