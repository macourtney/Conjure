(ns views.templates.delete-warning
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [views.templates.record-view :as record-view]))

(defview [table-metadata record]
  (html/html
    [:div { :class "article" }
      [:h2 (str "Deleting " (or (helpers/h (:name record)) (:id record)))]
      [:p "Are you sure you want to delete this record?"]
      (record-view/render-view request-map table-metadata record)
      (button-to "Delete" request-map { :action "delete", :params { :id record } })
      "&nbsp;"
      (link-to "Cancel" request-map { :action "list-records" })]))