(ns bindings.templates.ajax-save
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]))

(defbinding [model-name record]
  (when record
    (template-helper/with-template-action-request-map "ajax-record-row"
      (render-view model-name (template-helper/table-metadata model-name) record))))