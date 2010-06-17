(ns views.layouts.templates.links-sidebar
  (:use conjure.core.view.base)
  (:require [conjure.core.server.request :as request]
            [conjure.core.view.util :as view-util]
            [views.layouts.templates.links :as links]))

(defn
#^{ :doc "Creates a list link for use in the link sidebar." }
  list-link [layout-info]
    { :text "List", :url-for (view-util/merge-url-for-params layout-info { :action "list-records" }) })

(defn
#^{ :doc "Creates an add link for use in the link sidebar." } 
  add-link [layout-info]
    { :text "Add", 
      :url-for (view-util/merge-url-for-params layout-info { :action "add" }),
      :html-options { :id "add-action-link" } })

(def-view []
  (let [layout-info (request/layout-info)
        links (:links layout-info)]
      (links/render-body "Actions"
        (if links
          links
          [ (list-link layout-info)
            (add-link layout-info)]))))