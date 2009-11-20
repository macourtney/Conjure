(ns views.templates.add
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [views.templates.record-form :as record-form]))

(defview []
  (html/html
    [:h2 "Adding a new record"]
    (form-for request-map { :name "create", :url { :action "create" } }
      (str
        (record-form/render-view request-map {})
        (form-button "Create")
        (link-to "Cancel" request-map { :action "list-records" } )))))