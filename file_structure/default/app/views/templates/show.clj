(ns views.templates.show
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [conjure.util.string-utils :as conjure-str-utils]
            [views.templates.record-view :as record-view]))

(defview [model-name table-metadata record]
  (html/html
    [:div { :class "article" }
      [:h2 (or (helpers/h (:name record)) (str "Showing a " (conjure-str-utils/human-title-case model-name)))]
      (record-view/render-view request-map table-metadata record)
      (link-to "List" request-map { :action "list-records" })
      "&nbsp;"
      (link-to "Edit" request-map { :action "edit", :params { :id record } })]))