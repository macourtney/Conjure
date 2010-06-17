(ns bindings.templates.list-records
  (:use conjure.core.binding.base)
  (:require [helpers.template-helper :as template-helper]
            [views.templates.list-records :as list-records]))

(def-binding [model-name]
  (list-records/render-view
    model-name
    (template-helper/table-metadata model-name)
    (template-helper/all-records model-name)))