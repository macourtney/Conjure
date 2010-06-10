(ns bindings.templates.ajax-show
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]
            [conjure.server.request :as request]))

(defbinding [model-name]
  (when-let [id (request/id)]
    (let [table-metadata (template-helper/table-metadata model-name)]
      (template-helper/with-template-action-request-map "ajax-show"
        (render-view
          model-name
          table-metadata
          (template-helper/get-record model-name id)
          (inc (count table-metadata)))))))