(ns views.layouts.templates.breadcrumbs
  (:use conjure.view.base)
  (:require [clj-html.core :as html]))

(defview []
  (html/html
    [:p { :id "breadcrumbs"} 
      (let [layout-info (:layout-info request-map)
            original-controller (:controller layout-info)
            original-action (:action layout-info)
            id (:id request-map)]
        (html/htmli 
          "You are here: " 
          (link-to original-controller { :controller original-controller :action "index" }) 
          " &gt; " 
          (if id
            (html/htmli
              (link-to [:strong original-action] { :controller original-controller :action original-action })
              " &gt; " [:strong id])
            (link-to original-action { :controller original-controller :action original-action }))))]

    [:hr { :class "noscreen" }]))