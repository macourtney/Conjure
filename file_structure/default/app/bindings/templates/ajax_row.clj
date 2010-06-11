(ns bindings.templates.ajax-row
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]
            [conjure.server.request :as request]))

(def-binding [model-name]
  (let [id (request/id)]
    (when id
      (template-helper/with-template-action-request-map "ajax-record-row"
        (render-view
          model-name
          (template-helper/table-metadata model-name)
          (template-helper/get-record model-name id))))))