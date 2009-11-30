(ns views.templates.record-view
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [clj-html.utils :as utils]
            [conjure.util.string-utils :as conjure-str-utils]))

(defview [table-metadata record]
  (html/html
    (utils/domap-str [record-key (map #(keyword (. (:column_name %) toLowerCase)) table-metadata)]
      (html/html
        [:p (conjure-str-utils/human-readable record-key) ": " (helpers/h (get record record-key))]))))