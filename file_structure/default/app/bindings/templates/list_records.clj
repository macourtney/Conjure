(ns bindings.templates.list-records
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]))

(defbinding [request-map model-name]
  (render-view (template-helper/template-request-map request-map "list-records")
    model-name
    (template-helper/table-metadata model-name)
    (template-helper/all-records model-name)))