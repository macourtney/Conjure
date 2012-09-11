(ns views.templates.edit
  (:use conjure.view.base)
  (:require [conjure.util.request :as request]
            [helpers.template-helper :as template-helper]
            [views.templates.record-form :as record-form]))

(def-view []
  (let [model-name (request/service)
        table-metadata (template-helper/table-metadata model-name)
        record (request/record)]
    [:div { :class "article" }
      [:h2 (str "Editing " (or (:name record) (:id record) " a record"))]
      (form-for { :name "save", :action "save", :controller model-name }
        (list
          (hidden-field record :record :id)
          (record-form/render-body table-metadata record)
          (form-button "Save")
          (nbsp)
          (link-to "Cancel" { :action "show", :controller model-name, :params { :id record } } )))]))