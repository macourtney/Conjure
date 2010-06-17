(ns bindings.templates.ajax-edit
  (:use conjure.core.binding.base)
  (:require [conjure.core.server.request :as request]
            [helpers.template-helper :as template-helper]))

(def-binding [model-name]
  (let [id (request/id)]
    (if id
      (let [table-metadata (template-helper/table-metadata model-name)]
        (template-helper/with-template-action-request-map "ajax-edit"
          (render-view
            model-name
            table-metadata
            (template-helper/get-record model-name id)
            (inc (count table-metadata))))))))