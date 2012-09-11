(ns views.templates.delete-warning
  (:use conjure.view.base)
  (:require [conjure.util.request :as request]
            [helpers.template-helper :as template-helper]
            [views.templates.record-view :as record-view]))

(def-view []
  (let [model-name (request/service)
        table-metadata (template-helper/table-metadata model-name)
        record (request/record)]
    [:div { :class "article" }
      [:h2 (str "Deleting " (or (:name record) (:id record)))]
      [:p "Are you sure you want to delete this record?"]
      (record-view/render-body table-metadata record)
      (button-to "Delete" { :action "delete", :controller model-name, :params { :id record } })
      (nbsp)
      (link-to "Cancel" { :action "list-records", :controller model-name })]))