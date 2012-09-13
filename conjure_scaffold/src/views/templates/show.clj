(ns views.templates.show
  (:use conjure.view.base)
  (:require [conjure.util.request :as request]
            [clojure.tools.string-utils :as conjure-str-utils]
            [helpers.template-helper :as template-helper]
            [views.templates.record-view :as record-view]))

(def-view []
  (let [model-name (request/service)
        record (template-helper/get-record model-name (request/id))]
    [:div { :class "article" }
      [:h2 (or (:name record) (str "Showing a " (conjure-str-utils/human-title-case model-name)))]
      (record-view/render-body (template-helper/table-metadata model-name) record)
      (link-to "List" { :action "list-records", :service model-name })
      (nbsp)
      (link-to "Edit" { :action "edit", :service model-name, :params { :id record } })]))