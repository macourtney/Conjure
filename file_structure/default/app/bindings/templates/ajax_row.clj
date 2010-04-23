(ns bindings.templates.ajax-row
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]))

(defbinding [request-map model-name]
  (let [id (:id (:params request-map))]
    (if id
      (render-view { :layout nil } (template-helper/template-request-map request-map "record-row")
        model-name
        (template-helper/table-metadata model-name)
        (template-helper/get-record model-name id)))))