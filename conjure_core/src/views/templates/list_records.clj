(ns views.templates.list-records
  (:use conjure.core.view.base)
  (:require [com.reasonr.scriptjure :as scriptjure]
            [clojure.tools.logging :as logging]
            [clojure.tools.string-utils :as conjure-str-utils]
            [drift-db.core :as drift-db]
            [views.templates.record-form :as record-form]
            [views.templates.record-row :as record-row]))

(defn
#^{ :doc "Creates the header text from the given table-column." }
  header-name [column-metadata]
  (when-let [column-name (drift-db/column-name column-metadata)]
    [:th (conjure-str-utils/human-title-case 
      (conjure-str-utils/strip-ending 
        (conjure-str-utils/lower-case (name column-name))
        "_id"))]))

(defn header [table-metadata]
  (when table-metadata
    [:tr
      (map header-name (drift-db/columns table-metadata))
      [:th]]))

(def-view [model-name table-metadata records]
  (list
    [:div { :class "article" }
      [:h2 (str (conjure-str-utils/human-title-case model-name) " List")]
      [:table { :id "list-table" }
        (header table-metadata)
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
              (nbsp)
              (link-to "Cancel" { :action "list-records", :controller model-name, :html-options { :id "add-cancel" } } )))]
        (link-to "Add" { :action "add", :controller model-name, :html-options { :id "add-link" } } )]]
    [:script { :type "text/javascript" } 
      (keyword
        (scriptjure/js
          (initListAddLink "#add-link" "#add-form")))]))