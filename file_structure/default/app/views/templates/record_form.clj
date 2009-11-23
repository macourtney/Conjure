(ns views.templates.record-form
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.utils :as utils]))

(defview [table-metadata record]
  (html/html
    (utils/domap-str [table-column table-metadata]
      (let [field-name (. (:column_name table-column) toLowerCase)]
        (if (not (= field-name "id")) 
          (html/html
            [:p field-name ": " (text-field record "record" (keyword field-name))]))))))