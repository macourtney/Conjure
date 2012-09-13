(ns views.templates.add
  (:use conjure.view.base)
  (:require [clojure.tools.string-utils :as conjure-str-utils]
            [conjure.util.request :as request]
            [helpers.template-helper :as template-helper]
            [views.templates.record-form :as record-form]))

(def-view []
  (let [model-name (request/service)
        table-metadata (template-helper/table-metadata model-name)]
    [:div { :class "article" }
      [:h2 (str "Add a " (conjure-str-utils/human-title-case model-name))]
      (form-for { :name "create", :action "create", :service model-name }
        (list
          (record-form/render-body table-metadata {})
          (form-button "Create")
          (nbsp)
          (link-to "Cancel" { :action "list-records", :service model-name } )))]))