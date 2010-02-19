(ns views.layouts.templates.tabs
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.view.util :as view-util]))

(defn
#^{ :doc "Generates the clj-html structure for a given tab map." } 
  generate-tab [tab-map original-request-map]
  (let [location-controller (:controller original-request-map)
        tab-controller (or (:controller (:url-for tab-map)) location-controller)
        tab-url 
            (or 
              (:url tab-map) 
              (if (:url-for tab-map) 
                (view-util/url-for original-request-map (:url-for tab-map))))]
    [:li 
      { :id 
          (if 
            (or 
              (:is-active tab-map) 
              (and tab-controller location-controller (= tab-controller location-controller)))
            "active") }

      [:a { :href (or tab-url "#") } 
        (or (:text tab-map) "Tab") "<span class=\"tab-l\"></span><span class=\"tab-r\"></span>"]]))

(defview [tabs]
  (html/html
    [:div { :id "tabs", :class "noprint" }
      [:h3 { :class "noscreen" } "Navigation"]
      [:ul { :class "box" }
        (html/htmli (map #(generate-tab % (:layout-info request-map)) tabs))]

      [:hr { :class "noscreen" }]]))