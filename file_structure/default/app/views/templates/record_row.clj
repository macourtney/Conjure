(ns views.templates.record-row
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.view.util :as view-utils]
            [views.templates.record-cell :as record-cell]))

(defview [model-name table-metadata record]
  (let [row-id (str "row-" (:id record) )]
    (html/html
      [:tr { :id row-id } 
        (html/htmli 
          (map 
            #(record-cell/render-view request-map model-name record %) 
            (map #(keyword (. (get % :column_name) toLowerCase)) table-metadata)))
        [:td 
          (ajax-link-to "Delete" request-map 
            { :update (success-fn row-id :remove)
              :confirm (confirm-fn (str "Are you sure you want to delete the record with id: " (:id record)))
              :action "ajax-delete"
              :controller model-name,
              :params { :id record }
              :html-options
                { :href (view-utils/url-for request-map { :action "delete-warning", :controller model-name, :params { :id record } }) } })]])))