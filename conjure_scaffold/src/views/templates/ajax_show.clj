(ns views.templates.ajax-show
  (:use conjure.view.base)
  (:require [clojure.tools.string-utils :as conjure-str-utils]
            [conjure.util.request :as request]
            [helpers.template-helper :as template-helper]
            [views.templates.record-view :as record-view]))

(def-ajax-view [record]
  (let [model-name (request/service)
        table-metadata (template-helper/table-metadata model-name)
        column-count (inc (count table-metadata))
        row-id (str "row-" (:id record))]
    [:tr { :id row-id }
      [:td { :colspan column-count }
        [:div { :id (str "show-div-" (:id record)) }
          [:h3 (or (:name record) (str "Showing a " (conjure-str-utils/human-title-case model-name)))]
          (record-view/render-body table-metadata record)
          (ajax-link-to "Edit"
            { :update (success-fn row-id :replace)
              :action "ajax-edit"
              :service model-name
              :params { :id record } })
          (nbsp)
          (ajax-link-to "Hide"
            { :update (success-fn row-id :replace)
              :action "ajax-row"
              :service model-name
              :params { :id record } })]]]))