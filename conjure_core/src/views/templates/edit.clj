(ns views.templates.edit
  (:use conjure.core.view.base)
  (:require [views.templates.record-form :as record-form]
            [hiccup.core :as hiccup]))

(def-view [model-name table-metadata record]
  [:div { :class "article" }
    [:h2 (str "Editing " (hiccup/h (or (:name record) (:id record) " a record")))]
    (form-for { :name "save", :action "save", :controller model-name }
      (list
        (hidden-field record :record :id)
        (record-form/render-body table-metadata record)
        (form-button "Save")
        "&nbsp;"
        (link-to "Cancel" { :action "show", :controller model-name, :params { :id record } } )))])