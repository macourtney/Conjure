(ns bindings.templates.show
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]))

(defbinding [request-map model-name]
  (let [id (:id (:params request-map))]
    (render-view (template-helper/template-request-map request-map "show")
      model-name
      (template-helper/table-metadata model-name)
      (template-helper/get-record model-name id))))