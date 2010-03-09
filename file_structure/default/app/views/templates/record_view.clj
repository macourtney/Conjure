(ns views.templates.record-view
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.helpers :as helpers]
            [conjure.util.string-utils :as conjure-str-utils]))

(defn
#^{ :doc "Creates a view row for the table column in the given record." }
  view-row [request-map record table-column]
  (let [record-key-str (. (:column_name table-column) toLowerCase)
        record-key (keyword record-key-str)]
    (if (. record-key-str endsWith "_id")
      (let [belongs-to-model (conjure-str-utils/strip-ending record-key-str "_id")
            field-name (conjure-str-utils/human-title-case belongs-to-model)
            belongs-to-id (helpers/h (get record record-key))]
        [:p [:strong field-name] ": " 
          (link-to belongs-to-id request-map 
            { :controller belongs-to-model, 
              :action "show", 
              :params { :id belongs-to-id } })])
      [:p [:strong (conjure-str-utils/human-title-case record-key-str)] ": " (helpers/h (get record record-key))])))

(defview [table-metadata record]
  (html/html
    (map #(view-row request-map record %) table-metadata)))