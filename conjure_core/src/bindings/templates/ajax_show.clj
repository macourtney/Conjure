(ns bindings.templates.ajax-show
  (:use conjure.core.binding.base)
  (:require [conjure.core.server.request :as request]
            [helpers.template-helper :as template-helper]))

(def-binding [model-name]
  (when-let [id (request/id)]
    (let [table-metadata (template-helper/table-metadata model-name)]
      (template-helper/with-template-action-request-map "ajax-show"
        (render-view
          model-name
          table-metadata
          (template-helper/get-record model-name id)
          (inc (count table-metadata)))))))