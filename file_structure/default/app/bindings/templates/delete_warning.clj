(ns bindings.templates.delete-warning
  (:use conjure.binding.base)
  (:require [conjure.server.request :as request]
            [helpers.template-helper :as template-helper]
            [views.templates.delete-warning :as delete-warning]))

(def-binding [model-name]
  (let [id (request/id)]
    (delete-warning/render-view
      model-name
      (template-helper/table-metadata model-name)
      (if id (template-helper/get-record model-name id)))))