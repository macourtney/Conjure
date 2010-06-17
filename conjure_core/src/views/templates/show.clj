(ns views.templates.show
  (:use conjure.core.view.base)
  (:require [hiccup.core :as hiccup]
            [conjure.core.util.string-utils :as conjure-str-utils]
            [views.templates.record-view :as record-view]))

(def-view [model-name table-metadata record]
  [:div { :class "article" }
    [:h2 (or (hiccup/h (:name record)) (str "Showing a " (conjure-str-utils/human-title-case model-name)))]
    (record-view/render-body table-metadata record)
    (link-to "List" { :action "list-records", :controller model-name })
    "&nbsp;"
    (link-to "Edit" { :action "edit", :controller model-name, :params { :id record } })])