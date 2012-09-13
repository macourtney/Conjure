(ns views.templates.ajax-edit
  (:use conjure.view.base)
  (:require [clojure.tools.string-utils :as conjure-str-utils]
            [conjure.util.request :as request]
            [helpers.template-helper :as template-helper]
            [views.templates.record-form :as record-form]))

(def-ajax-view [record]
  (let [model-name (request/service)
        table-metadata (template-helper/table-metadata model-name)
        column-count (inc (count table-metadata))
        row-id (str "row-" (:id record))]
    [:tr { :id row-id }
      [:td { :colspan column-count }
        [:div { :id (str "show-div-" (:id record)) }
          [:h3 (or (:name record) (str "Editing a " (conjure-str-utils/human-title-case model-name)))]
          (ajax-form-for
              { :name "ajax-save", 
                :action "ajax-save",
                :service model-name,
                :update (success-fn row-id :replace) }
            (list
              (hidden-field record :record :id)
              (record-form/render-body table-metadata record)
              (form-button "Save")
              (nbsp)
              (ajax-link-to "Hide"
                { :update (success-fn row-id :replace)
                  :action "ajax-row"
                  :service model-name
                  :params { :id record } })))]]]))