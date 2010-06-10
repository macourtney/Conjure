(ns bindings.templates.list-records
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]))

(defbinding [model-name]
  (template-helper/with-template-action-request-map "list-records"
    (render-view model-name (template-helper/table-metadata model-name) (template-helper/all-records model-name))))