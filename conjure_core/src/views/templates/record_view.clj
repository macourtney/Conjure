(ns views.templates.record-view
  (:use conjure.core.view.base)
  (:require [conjure.core.util.string-utils :as conjure-str-utils]
            [hiccup.core :as hiccup]))

(defn
#^{ :doc "Creates a view row for the table column in the given record." }
  view-row [record table-column]
  (let [record-key-str (. (:field table-column) toLowerCase)
        record-key (keyword record-key-str)]
    (if (. record-key-str endsWith "_id")
      (let [belongs-to-model (conjure-str-utils/strip-ending record-key-str "_id")
            field-name (conjure-str-utils/human-title-case belongs-to-model)
            belongs-to-id (hiccup/h (get record record-key))]
        [:p [:strong field-name] ": " 
          (link-to belongs-to-id
            { :controller belongs-to-model, 
              :action "show", 
              :params { :id belongs-to-id } })])
      [:p [:strong (conjure-str-utils/human-title-case record-key-str)] ": " (hiccup/h (get record record-key))])))

(def-view [table-metadata record]
  (map #(view-row record %) table-metadata))