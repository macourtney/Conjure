(ns views.templates.show
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]))

(defview [record]
  (html/html
    (if (:name record) [:h2 (:name record)])
    (domap-str [record-key (keys record)]
      (html/html
        [:p record-key ": " (helpers/h (get record record-key))]
        [:br]))
    (link-to "List" { :action "list-records" :controller (:controller request-map) })))