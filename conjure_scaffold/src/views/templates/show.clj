(ns views.templates.show
  (:use conjure.view.base)
  (:require [conjure.util.request :as request]
            [clojure.tools.string-utils :as conjure-str-utils]
            [helpers.template-helper :as template-helper]
            [views.templates.record-view :as record-view]))

(def-view []
  (let [model-name (request/service)
        table-metadata (template-helper/table-metadata model-name)
        record (request/record)]
    [:div { :class "article" }
      [:h2 (or (:name record) (str "Showing a " (conjure-str-utils/human-title-case model-name)))]
      (record-view/render-body table-metadata record)
      (link-to "List" { :action "list-records", :controller model-name })
      (nbsp)
      (link-to "Edit" { :action "edit", :controller model-name, :params { :id record } })]))