(ns bindings.templates.edit
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]))

(defbinding [request-map model-name]
  (let [id (:id (:params request-map))]
    (render-view (template-helper/template-request-map request-map "edit")
      model-name
      (template-helper/table-metadata model-name)
      (template-helper/get-record model-name id))))