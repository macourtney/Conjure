(ns views.templates.edit
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [views.templates.record-form :as record-form]))

(defview [record]
  (html/html
    [:h2 (str "Editing " (helpers/h (or (:name record) (:id record) " a record")))]
    (form-for request-map { :name "create", :url { :action "create" } }
      (record-form/render-view request-map record)
      (form-button "Create")
      (link-to "Cancel" request-map { :action "list-records" } ))))