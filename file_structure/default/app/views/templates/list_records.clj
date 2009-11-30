(ns views.templates.list-records
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [clj-html.utils :as utils]
            [conjure.util.string-utils :as conjure-str-utils]))

(defview [model-name table-metadata records]
  (html/html 
    [:h2 (str (conjure-str-utils/human-readable model-name) " List")]
    [:table
      [:tr
        (utils/domap-str [table-column table-metadata]
          (let [field-name (. (:column_name table-column) toLowerCase)]
            (html/html
              [:th (conjure-str-utils/human-readable field-name)])))
        [:th]]
      (utils/domap-str [record records]
        (html/html 
          [:tr 
            (html/htmli 
              (map 
                (fn [record-key] [:td (link-to-if (= :id record-key) (helpers/h (get record record-key)) request-map { :action "show", :id (:id record) })]) 
                (map #(keyword (. (get % :column_name) toLowerCase)) table-metadata)))
            [:td (link-to "Delete" request-map { :action "delete-warning", :id record })]]))]
    (link-to "Add" request-map { :action "add" } )))