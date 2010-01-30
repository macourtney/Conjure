(ns views.templates.record-row
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.view.util :as view-utils]
            [views.templates.record-cell :as record-cell]))

(defview [table-metadata record]
  (let [row-id (str "row-" (:id record) )]
    (html/html
      [:tr { :id row-id } 
        (html/htmli 
          (map 
            #(record-cell/render-view request-map record %) 
            (map #(keyword (. (get % :column_name) toLowerCase)) table-metadata)))
        [:td 
          (link-to-remote "Delete" request-map 
            { :update (success-fn row-id :remove)
              :confirm (confirm-fn (str "Are you sure you want to delete the record with id: " (:id record)))
              :action "ajax-delete"
              :id record
              :html-options
                { :href (view-utils/url-for request-map { :action "delete-warning", :id record }) } })]])))