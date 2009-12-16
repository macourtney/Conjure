(ns views.templates.record-view
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [clj-html.utils :as utils]
            [conjure.util.string-utils :as conjure-str-utils]))

(defview [table-metadata record]
  (html/html
    (utils/domap-str [record-key (map #(keyword (. (:column_name %) toLowerCase)) table-metadata)]
      (html/htmli
        (let [record-key-str (conjure-str-utils/str-keyword record-key)]
          (if (. record-key-str endsWith "_id")
            (let [belongs-to-model (conjure-str-utils/strip-ending record-key-str "_id")
                  field-name (conjure-str-utils/human-readable belongs-to-model)
                  belongs-to-id (helpers/h (get record record-key))]
              [:p field-name ": " (link-to belongs-to-id request-map { :controller belongs-to-model, :action "show", :id belongs-to-id })])
            [:p (conjure-str-utils/human-readable record-key-str) ": " (helpers/h (get record record-key))]))))))