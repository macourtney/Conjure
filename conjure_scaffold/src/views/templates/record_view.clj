(ns views.templates.record-view
  (:use conjure.view.base)
  (:require [clojure.tools.loading-utils :as conjure-loading-utils]
            [clojure.tools.string-utils :as conjure-str-utils]
            [drift-db.core :as drift-db]))

(defn view-id-row [record record-key]
  (when (and record record-key)
    (let [record-key-str (name record-key)
          belongs-to-model (conjure-str-utils/strip-ending record-key-str "-id")
          field-name (conjure-str-utils/human-title-case belongs-to-model)
          belongs-to-id (get record record-key)]
      [:p [:strong field-name] ": " 
        (link-to belongs-to-id
          { :controller belongs-to-model, 
            :action "show", 
            :params { :id belongs-to-id } })])))

(defn
#^{ :doc "Creates a view row for the table column in the given record." }
  view-row [record column-metadata]
  (when (and record column-metadata)
    (let [record-key (drift-db/column-name column-metadata)
          record-key-str (name record-key)]
      (if (. record-key-str endsWith "-id")
        (view-id-row record record-key)
        [:p [:strong (conjure-str-utils/human-title-case record-key-str)] ": " (get record record-key)]))))

(def-view [table-metadata record]
  (map #(view-row record %) (drift-db/columns table-metadata)))