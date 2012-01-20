(ns views.templates.add
  (:use conjure.core.view.base)
  (:require [clojure.tools.string-utils :as conjure-str-utils]
            [views.templates.record-form :as record-form]))

(def-view [model-name table-metadata]
  [:div { :class "article" }
    [:h2 (str "Add a " (conjure-str-utils/human-title-case model-name))]
    (form-for { :name "create", :action "create", :controller model-name }
      (list
        (record-form/render-body table-metadata {})
        (form-button "Create")
        (nbsp)
        (link-to "Cancel" { :action "list-records", :controller model-name } )))])