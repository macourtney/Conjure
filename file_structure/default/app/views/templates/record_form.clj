(ns views.templates.record-form
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [clj-html.utils :as utils]
            [conjure.util.string-utils :as conjure-str-utils]
            [conjure.model.util :as model-util]
            [conjure.util.loading-utils :as loading-utils]))

(defn editor [record field-name]
  (let [record-name "record"]
    (if (. field-name endsWith "_id")
      (select-tag record record-name field-name 
        { :option-map (options-from-model 
          { :model (model-util/to-model-name (loading-utils/underscores-to-dashes field-name))
            :blank true
            :name-key :id }) })
      (text-field record record-name (keyword field-name)))))

(defview [table-metadata record]
  (html/html
    (utils/domap-str [table-column table-metadata]
      (let [field-name (. (:column_name table-column) toLowerCase)]
        (if (not (= field-name "id")) 
          (html/html
            [:p (conjure-str-utils/human-readable field-name) ": " (editor record field-name)]))))))