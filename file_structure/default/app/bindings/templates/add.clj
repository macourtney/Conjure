(ns bindings.templates.add
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]))

(defbinding [model-name]
  (template-helper/with-template-action-request-map "add"
    (render-view model-name (template-helper/table-metadata model-name))))