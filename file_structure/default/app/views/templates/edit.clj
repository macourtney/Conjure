(ns views.templates.edit
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [views.templates.record-form :as record-form]))

(defview [table-metadata record]
  (html/html
    [:div { :class "article" }
      [:h2 (str "Editing " (helpers/h (or (:name record) (:id record) " a record")))]
      (form-for request-map { :name "save", :action "save" }
        (str
          (hidden-field record :record :id)
          (record-form/render-view request-map table-metadata record)
          (form-button "Save")
          "&nbsp;"
          (link-to "Cancel" request-map { :action "show", :params { :id record } } )))]))