(ns bindings.templates.edit
  (:use conjure.binding.base)
  (:require [conjure.server.request :as request]
            [helpers.template-helper :as template-helper]
            [views.templates.edit :as edit]))

(def-binding [model-name]
  (let [id (request/id)]
    (edit/render-view
      model-name
      (template-helper/table-metadata model-name)
      (template-helper/get-record model-name id))))