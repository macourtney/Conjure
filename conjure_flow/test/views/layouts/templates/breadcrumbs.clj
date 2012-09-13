(ns views.layouts.templates.breadcrumbs
  (:use conjure.view.base)
  (:require [clojure.tools.string-utils :as conjure-str-utils]
            [conjure.util.request :as request]))

(def-view []
  (list
    [:p { :id "breadcrumbs"} 
      (let [layout-info (request/layout-info)
            original-controller (:service layout-info)
            original-action (:action layout-info)
            id (:id (:params layout-info))]
        (list 
          "You are here: " 
          (link-to 
            [:strong (conjure-str-utils/human-title-case original-controller)] 
            { :service original-controller, :action "index", :params {} }) 
          " &gt; " 
          (if id
            (list
              (link-to 
                [:strong (conjure-str-utils/human-title-case original-action)] 
                { :service original-controller, :action original-action, :params { :id id } })
              " &gt; " [:strong id])
            (link-to 
              [:strong (conjure-str-utils/human-title-case original-action)] 
              { :service original-controller, :action original-action, :params {} }))))]
  
    [:hr { :class "noscreen" }]))