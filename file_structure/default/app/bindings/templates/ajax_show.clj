(ns bindings.templates.ajax-show
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]))

(defbinding [request-map model-name]
  (let [id (:id (:params request-map))]
    (if id
      (let [table-metadata (template-helper/table-metadata model-name)]
        (render-view { :layout nil } (template-helper/template-request-map request-map "ajax-show")
          model-name
          table-metadata
          (template-helper/get-record model-name id)
          (inc (count table-metadata)))))))