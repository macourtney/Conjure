(ns views.templates.record-form
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.util.string-utils :as conjure-str-utils]
            [conjure.model.util :as model-util]
            [conjure.util.loading-utils :as loading-utils]))

(defn
  #^{ :doc "Creates the editor for the given field in the given record." }
  editor [record field-name]
  (let [record-name "record"]
    (if (. field-name endsWith "_id")
      (select-tag record record-name field-name 
        { :option-map (options-from-model 
          { :model (model-util/to-model-name (loading-utils/underscores-to-dashes field-name))
            :blank true
            :name-key :id }) })
      (text-field record record-name (keyword field-name)))))

(defn
#^{ :doc "Creates the form row for the table column in the given record." }
  form-row [record table-column]
  (let [field-name (. (:column_name table-column) toLowerCase)]
    (if (not (= field-name "id")) 
      [:p [:strong (conjure-str-utils/human-title-case field-name)] ": " (editor record field-name)])))

(defview [table-metadata record]
  (html/html
    (map #(form-row record %) table-metadata)))