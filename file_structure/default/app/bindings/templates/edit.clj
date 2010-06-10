(ns bindings.templates.edit
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]
            [conjure.server.request :as request]))

(defbinding [model-name]
  (let [id (request/id)]
    (template-helper/with-template-action-request-map "edit"
      (render-view
        model-name
        (template-helper/table-metadata model-name)
        (template-helper/get-record model-name id)))))