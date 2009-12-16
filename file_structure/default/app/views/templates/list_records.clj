(ns views.templates.list-records
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [clj-html.utils :as utils]
            [conjure.util.string-utils :as conjure-str-utils]))

(defn
#^{ :doc "Returns a table cell for the value at the given record-key in record. If record-key is :id then this function 
links to the show page for this record. If the record key ends with \"_id\", then this function assumes it is a 
belongs-to field and links to the corresponding show page for the record it points to." }
  cell-value [request-map record record-key]
  (if (= :id record-key)
    [:td (link-to (helpers/h (get record record-key)) request-map { :action "show", :id (:id record) })]
    (let [record-key-str (conjure-str-utils/str-keyword record-key)]
      (if (. record-key-str endsWith "_id")
        (let [belongs-to-model (conjure-str-utils/strip-ending record-key-str "_id")
              field-name (conjure-str-utils/human-readable belongs-to-model)
              belongs-to-id (helpers/h (get record record-key))]
          [:td (link-to belongs-to-id request-map { :controller belongs-to-model, :action "show", :id belongs-to-id })])
        [:td (helpers/h (get record record-key))]))))

(defview [model-name table-metadata records]
  (html/html 
    [:div { :class "article" }
      [:h2 (str (conjure-str-utils/human-readable model-name) " List")]
      [:table
        [:tr
          (utils/domap-str [table-column table-metadata]
            (let [field-name (conjure-str-utils/strip-ending (. (:column_name table-column) toLowerCase) "_id")]
              (html/html
                [:th (conjure-str-utils/human-readable field-name)])))
          [:th]]
        (utils/domap-str [record records]
          (html/html 
            [:tr 
              (html/htmli 
                (map 
                  #(cell-value request-map record %) 
                  (map #(keyword (. (get % :column_name) toLowerCase)) table-metadata)))
              [:td (link-to "Delete" request-map { :action "delete-warning", :id record })]]))]
      (link-to "Add" request-map { :action "add" } )]))