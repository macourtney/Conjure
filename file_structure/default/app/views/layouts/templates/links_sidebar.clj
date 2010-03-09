(ns views.layouts.templates.links-sidebar
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [views.layouts.templates.links :as links]
            [conjure.view.util :as view-util]))

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

(defview []
  (let [layout-info (:layout-info request-map)
        links (:links layout-info)]
    (html/html
      (links/render-view request-map "Actions"
        (if links
          links
          [ (list-link layout-info)
            (add-link layout-info)])))))