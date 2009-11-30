(ns views.templates.delete-warning
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [views.templates.record-view :as record-view]))

(defview [table-metadata record]
  (html/html
    [:h2 (or (:name record) (str "Deleting " (:id record)))]
    [:p "Are you sure you want to delete this record?"]
    (record-view/render-view request-map table-metadata record)
    (button-to "Delete" request-map { :action "delete", :id record })
    "&nbsp;"
    (link-to "Cancel" { :action "list-records", :controller (:controller request-map) })))