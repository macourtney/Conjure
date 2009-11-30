(ns views.templates.show
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.util.string-utils :as conjure-str-utils]
            [views.templates.record-view :as record-view]))

(defview [model-name table-metadata record]
  (html/html
    [:h2 (or (:name record) (str "Showing a " (conjure-str-utils/human-readable model-name)))]
    (record-view/render-view request-map table-metadata record)
    (link-to "List" { :action "list-records" :controller (:controller request-map) })
    "&nbsp;"
    (link-to "Edit" { :action "edit", :id record, :controller (:controller request-map) })))