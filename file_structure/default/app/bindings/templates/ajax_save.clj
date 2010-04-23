(ns bindings.templates.ajax-save
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]))

(defbinding [request-map model-name record]
  (if record
    (render-view { :layout nil } (template-helper/template-request-map request-map "record-row")
      model-name
      (template-helper/table-metadata model-name)
      record)))