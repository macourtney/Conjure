(ns bindings.templates.ajax-row
  (:use conjure.core.binding.base)
  (:require [conjure.core.server.request :as request]
            [helpers.template-helper :as template-helper]))

(def-binding [model-name]
  (let [id (request/id)]
    (when id
      (template-helper/with-template-action-request-map "ajax-record-row"
        (render-view
          model-name
          (template-helper/table-metadata model-name)
          (template-helper/get-record model-name id))))))