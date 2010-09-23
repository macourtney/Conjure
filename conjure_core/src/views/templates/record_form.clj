(ns views.templates.record-form
  (:use conjure.core.view.base)
  (:require [clojure_util.loading-utils :as loading-utils]
            [clojure_util.string-utils :as conjure-str-utils]
            [conjure.core.model.util :as model-util]))

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
  (let [field-name (.toLowerCase (:field table-column))]
    (if (not (= field-name "id")) 
      [:p [:strong (conjure-str-utils/human-title-case field-name)] ": " (editor record field-name)])))

(def-view [table-metadata record]
  (map #(form-row record %) table-metadata))