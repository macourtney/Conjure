(ns views.layouts.templates.breadcrumbs
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.util.string-utils :as conjure-str-utils]))

(defview []
  (html/html
    [:p { :id "breadcrumbs"} 
      (let [layout-info (:layout-info request-map)
            original-controller (:controller layout-info)
            original-action (:action layout-info)
            id (:id (:params layout-info))]
        (html/htmli 
          "You are here: " 
          (link-to 
            [:strong (conjure-str-utils/human-title-case original-controller)] 
            { :controller original-controller :action "index" }) 
          " &gt; " 
          (if id
            (html/htmli
              (link-to 
                [:strong (conjure-str-utils/human-title-case original-action)] 
                { :controller original-controller, :action original-action, :id id })
              " &gt; " [:strong id])
            (link-to 
              [:strong (conjure-str-utils/human-title-case original-action)] 
              { :controller original-controller :action original-action }))))]

    [:hr { :class "noscreen" }]))