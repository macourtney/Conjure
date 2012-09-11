(ns views.templates.record-row
  (:use conjure.view.base)
  (:require [conjure.util.conjure-utils :as conjure-utils]
            [conjure.util.request :as request]
            [drift-db.core :as drift-db]
            [helpers.template-helper :as template-helper]
            [views.templates.record-cell :as record-cell]))

(defn column-names [table-metadata]
  (map drift-db/column-name (drift-db/columns table-metadata)))

(def-view [record]
  (let [model-name (request/service)
        table-metadata (template-helper/table-metadata model-name)
        row-id (str "row-" (:id record) )]
    [:tr { :id row-id } 
      (map 
        #(record-cell/render-body model-name record %) 
        (column-names table-metadata))
      [:td 
        (ajax-link-to "Delete"
          { :update (success-fn row-id :remove)
            :confirm (confirm-fn (str "Are you sure you want to delete the record with id: " (:id record)))
            :action "ajax-delete"
            :controller model-name,
            :params { :id record }
            :html-options
              { :href (conjure-utils/url-for 
                        { :action "delete-warning", 
                          :controller model-name, 
                          :params { :id record } }) } })]]))