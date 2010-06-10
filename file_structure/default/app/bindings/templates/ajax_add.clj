(ns bindings.templates.ajax-add
  (:use conjure.binding.base)
  (:require [helpers.template-helper :as template-helper]))

(defbinding [request-map model-name created-record]
  (template-helper/with-template-action-request-map "ajax-record-row"
    (render-view
      model-name
      (template-helper/table-metadata model-name)
      created-record)))