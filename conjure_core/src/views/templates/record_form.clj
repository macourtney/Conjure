(ns views.templates.record-form
  (:use conjure.core.view.base)
  (:require [clojure.tools.loading-utils :as loading-utils]
            [clojure.tools.string-utils :as conjure-str-utils]
            [conjure.core.model.util :as model-util]
            [drift-db.core :as drift-db]))

(defn
  #^{ :doc "Creates the editor for the given field in the given record." }
  editor [record field-name]
  (when (and record field-name)
    (let [record-name "record"
          field-name-str (name field-name)]
      (if (conjure-str-utils/ends-with? field-name-str "_id")
        (select-tag record record-name field-name 
          { :option-map (options-from-model 
            { :model (model-util/to-model-name (loading-utils/underscores-to-dashes field-name-str))
              :blank true
              :name-key :id }) })
        (text-field record record-name field-name)))))

(defn
#^{ :doc "Creates the form row for the table column in the given record." }
  form-row [record table-column]
  (let [field-name (drift-db/column-name table-column)
        field-name-str (name field-name)]
    (when (not (= field-name-str "id")) 
      [:p [:strong (conjure-str-utils/human-title-case field-name-str)] ": " (editor record field-name)])))

(def-view [table-metadata record]
  (map #(form-row record %) (drift-db/columns table-metadata)))