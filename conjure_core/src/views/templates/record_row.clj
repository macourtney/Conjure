(ns views.templates.record-row
  (:use conjure.core.view.base)
  (:require [conjure.core.view.util :as view-utils]
            [drift-db.core :as drift-db]
            [views.templates.record-cell :as record-cell]))

(defn column-names [table-metadata]
  (map drift-db/column-name (drift-db/columns table-metadata)))

(def-view [model-name table-metadata record]
  (let [row-id (str "row-" (:id record) )]
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
              { :href (view-utils/url-for 
                        { :action "delete-warning", 
                          :controller model-name, 
                          :params { :id record } }) } })]]))