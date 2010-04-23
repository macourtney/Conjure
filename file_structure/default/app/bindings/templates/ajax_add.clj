(ns bindings.templates.ajax-add
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]))

(defbinding [request-map model-name created-record]
  (render-view { :layout nil } (template-helper/template-request-map request-map "record-row")
    model-name
    (template-helper/table-metadata model-name)
    created-record))