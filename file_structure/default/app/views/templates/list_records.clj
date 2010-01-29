(ns views.templates.list-records
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.utils :as utils]
            [com.reasonr.scriptjure :as scriptjure]
            [conjure.util.string-utils :as conjure-str-utils]
            [views.templates.record-form :as record-form]
            [views.templates.record-row :as record-row]))

(defview [model-name table-metadata records]
  (html/html 
    [:div { :class "article" }
      [:h2 (str (conjure-str-utils/human-title-case model-name) " List")]
      [:table { :id "list-table" }
        [:tr
          (utils/domap-str [table-column table-metadata]
            (let [field-name (conjure-str-utils/strip-ending (. (:column_name table-column) toLowerCase) "_id")]
              (html/html
                [:th (conjure-str-utils/human-title-case field-name)])))
          [:th]]
        (utils/domap-str [record records]
          (record-row/render-view request-map table-metadata record))]
      [:div { :id "add" }
        [:div { :id "add-form", :style "display: none" }
          (remote-form-for request-map 
            { :name "record",
              :url { :action "ajax-add" },
              :update '(addFormSuccess "#list-table" "#add-link" "#add-form") }
            (str 
              (record-form/render-view request-map table-metadata {})
              (form-button "Create")
              "&nbsp;"
              (link-to "Cancel" request-map { :action "list-records", :html-options { :id "add-cancel" } } )))]
        (link-to "Add" request-map { :action "add", :html-options { :id "add-link" } } )]]
    [:script { :type "text/javascript" } 
      (scriptjure/js
        (initListAddLink "#add-link" "#add-form"))]))