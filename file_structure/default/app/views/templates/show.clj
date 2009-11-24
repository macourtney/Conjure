(ns views.templates.show
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [clj-html.utils :as utils]
            [conjure.util.string-utils :as conjure-str-utils]))

(defview [model-name table-metadata record]
  (html/html
    [:h2 (or (:name record) (str "Showing a " (conjure-str-utils/human-readable model-name)))]
    (utils/domap-str [record-key (map #(keyword (. (:column_name %) toLowerCase)) table-metadata)]
      (html/html
        [:p (conjure-str-utils/human-readable record-key) ": " (helpers/h (get record record-key))]
        [:br]))
    (link-to "List" { :action "list-records" :controller (:controller request-map) })
    "&nbsp;"
    (link-to "Edit" { :action "edit", :id record, :controller (:controller request-map) })))