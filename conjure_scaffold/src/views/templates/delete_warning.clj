(ns views.templates.delete-warning
  (:use conjure.view.base)
  (:require [conjure.util.request :as request]
            [helpers.template-helper :as template-helper]
            [views.templates.record-view :as record-view]))

(def-view []
  (let [model-name (request/service)
        record (template-helper/get-record model-name (request/id))]
    [:div { :class "article" }
      [:h2 (str "Deleting " (or (:name record) (:id record)))]
      [:p "Are you sure you want to delete this record?"]
      (record-view/render-body (template-helper/table-metadata model-name) record)
      (button-to "Delete" { :action "delete", :service model-name, :params { :id record } })
      (nbsp)
      (link-to "Cancel" { :action "list-records", :service model-name })]))