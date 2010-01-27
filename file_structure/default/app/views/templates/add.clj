(ns views.templates.add
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [conjure.util.string-utils :as conjure-str-utils]
            [views.templates.record-form :as record-form]))

(defview [model-name table-metadata]
  (html/html
    [:div { :class "article" }
      [:h2 (str "Add a " (conjure-str-utils/human-title-case model-name))]
      (form-for request-map { :name "create", :url { :action "create" } }
        (str
          (record-form/render-view request-map table-metadata {})
          (form-button "Create")
          "&nbsp;"
          (link-to "Cancel" request-map { :action "list-records" } )))]))