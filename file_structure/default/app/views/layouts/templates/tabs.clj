(ns views.layouts.templates.tabs
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.view.util :as view-util]))

(defview [tabs]
  (let [original-request-map (:layout-info request-map)
        location-controller (:controller original-request-map)]
    (html/html
      [:div { :id "tabs", :class "noprint" }
        [:h3 { :class "noscreen" } "Navigation"]
        [:ul { :class "box" }
          (html/htmli
            (map 
              (fn [tab] 
                (let [tab-controller (or (:controller (:url-for tab)) location-controller)
                      tab-url (or (:url tab) (if (:url-for tab) (view-util/url-for original-request-map (:url-for tab))))]
                  [:li { :id (if (or (:is-active tab) (if (and tab-controller location-controller) (= tab-controller location-controller))) "active") } 
                    [:a { :href (or tab-url "#") } 
                      (or (:text tab) "Tab") "<span class=\"tab-l\"></span><span class=\"tab-r\"></span>"]]))
              tabs))]
  
        [:hr { :class "noscreen" }]])))