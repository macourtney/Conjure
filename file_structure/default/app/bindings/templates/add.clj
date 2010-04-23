(ns bindings.templates.add
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]))

(defbinding [request-map model-name]
  (render-view (template-helper/template-request-map request-map "add")
    model-name
    (template-helper/table-metadata model-name)))