(ns views.layouts.templates.breadcrumbs
  (:use conjure.view.base)
  (:require [conjure.util.request :as request]
            [clojure.tools.string-utils :as conjure-str-utils]))



(def-view []
  (list
    [:p { :id "breadcrumbs"} 
      (let [layout-info (request/layout-info)
            original-service (request/service layout-info)
            original-action (request/action layout-info)
            id (request/id layout-info)]
        (list 
          "You are here: " 
          (link-to 
            [:strong (conjure-str-utils/human-title-case original-service)] 
            { :service original-service, :action "index", :params {} }) 
          (keyword " &gt; ")
          (link-to 
            [:strong (conjure-str-utils/human-title-case original-action)] 
            { :service original-service, :action original-action, :params { :id id } })
          (when id
            (list (keyword " &gt; ") [:strong id]))))]

    [:hr { :class "noscreen" }]))