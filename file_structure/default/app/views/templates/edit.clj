(ns views.templates.edit
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [views.templates.record-form :as record-form]))

(def-view [model-name table-metadata record]
  [:div { :class "article" }
    [:h2 (str "Editing " (helpers/h (or (:name record) (:id record) " a record")))]
    (form-for { :name "save", :action "save", :controller model-name }
      (list
        (hidden-field record :record :id)
        (record-form/render-body table-metadata record)
        (form-button "Save")
        "&nbsp;"
        (link-to "Cancel" { :action "show", :controller model-name, :params { :id record } } )))])