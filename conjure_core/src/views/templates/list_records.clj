(ns views.templates.list-records
  (:use conjure.core.view.base)
  (:require [com.reasonr.scriptjure :as scriptjure]
            [conjure.core.util.string-utils :as conjure-str-utils]
            [views.templates.record-form :as record-form]
            [views.templates.record-row :as record-row]))

(defn
#^{ :doc "Creates the header text from the given table-column." }
  header-name [table-column]
  (logging/debug (str "table-column: " table-column))
  [:th (conjure-str-utils/human-title-case 
    (conjure-str-utils/strip-ending 
      (.toLowerCase (:field table-column)) 
      "_id"))])

(def-view [model-name table-metadata records]
  (list
    [:div { :class "article" }
      [:h2 (str (conjure-str-utils/human-title-case model-name) " List")]
      [:table { :id "list-table" }
        [:tr
          (map header-name table-metadata)
          [:th]]
        (map #(record-row/render-body model-name table-metadata %) records)]
      [:div { :id "add" }
        [:div { :id "add-form", :style "display: none" }
          (ajax-form-for
            { :name "record",
              :action "ajax-add",
              :controller model-name,
              :update '(addFormSuccess "#list-table" "#add-link" "#add-form") }
            (list 
              (record-form/render-body table-metadata {})
              (form-button "Create")
              "&nbsp;"
              (link-to "Cancel" { :action "list-records", :controller model-name, :html-options { :id "add-cancel" } } )))]
        (link-to "Add" { :action "add", :controller model-name, :html-options { :id "add-link" } } )]]
    [:script { :type "text/javascript" } 
      (scriptjure/js
        (initListAddLink "#add-link" "#add-form"))]))