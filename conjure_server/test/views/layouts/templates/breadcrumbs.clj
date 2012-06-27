(ns views.layouts.templates.breadcrumbs
  (:use conjure.core.view.base)
  (:require [conjure.core.server.request :as request]
            [clojure.tools.string-utils :as conjure-str-utils]))

(def-view []
  (list
    [:p { :id "breadcrumbs"} 
      (let [layout-info (request/layout-info)
            original-controller (:controller layout-info)
            original-action (:action layout-info)
            id (:id (:params layout-info))]
        (list 
          "You are here: " 
          (link-to 
            [:strong (conjure-str-utils/human-title-case original-controller)] 
            { :controller original-controller, :action "index", :params {} }) 
          " &gt; " 
          (if id
            (list
              (link-to 
                [:strong (conjure-str-utils/human-title-case original-action)] 
                { :controller original-controller, :action original-action, :params { :id id } })
              " &gt; " [:strong id])
            (link-to 
              [:strong (conjure-str-utils/human-title-case original-action)] 
              { :controller original-controller, :action original-action, :params {} }))))]
  
    [:hr { :class "noscreen" }]))