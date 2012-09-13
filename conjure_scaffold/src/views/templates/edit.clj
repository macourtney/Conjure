(ns views.templates.edit
  (:use conjure.view.base)
  (:require [conjure.util.request :as request]
            [helpers.template-helper :as template-helper]
            [views.templates.record-form :as record-form]))

(def-view []
  (let [model-name (request/service)
        record (template-helper/get-record model-name (request/id))]
    [:div { :class "article" }
      [:h2 (str "Editing " (or (:name record) (:id record) " a record"))]
      (form-for { :name "save", :action "save", :service model-name }
        (list
          (hidden-field record :record :id)
          (record-form/render-body (template-helper/table-metadata model-name) record)
          (form-button "Save")
          (nbsp)
          (link-to "Cancel" { :action "show", :service model-name, :params { :id record } } )))]))