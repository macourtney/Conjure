(ns bindings.generic.ajax-show
  (:use conjure.binding.base)
  (:require [conjure.model.util :as model-util]))

(defbinding [request-map model-name]
  (let [id (:id (:params request-map))]
    (if id
      (let [model-namespace (find-ns (symbol (model-util/model-namespace model-name)))
            table-metadata ((ns-resolve  model-namespace 'table-metadata))]
        (render-view { :layout nil } (merge request-map { :controller "templates" :action "ajax-show" })
          model-name
          table-metadata
          ((ns-resolve  model-namespace 'get-record) id)
          (inc (count table-metadata)))))))