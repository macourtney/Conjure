(ns views.templates.show
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [clj-html.utils :as utils]))

(defview [record]
  (html/html
    (if (:name record) [:h2 (:name record)])
    (utils/domap-str [record-key (keys record)]
      (html/html
        [:p record-key ": " (helpers/h (get record record-key))]
        [:br]))
    (link-to "List" { :action "list-records" :controller (:controller request-map) })
    (link-to "Edit" { :action "edit", :id record, :controller (:controller request-map) })))