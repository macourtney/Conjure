(ns views.layouts.templates.tabs
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.view.util :as view-util]))

(defview [tabs]
  (let [location-url (view-util/url-for (:layout-info request-map))]
    (html/html
      [:div { :id "tabs", :class "noprint" }
        [:h3 { :class "noscreen" } "Navigation"]
        [:ul { :class "box" }
          (html/htmli
            (map 
              (fn [tab] 
                (let [tab-url (or (:url tab) (if (:url-for tab) (view-util/url-for request-map (:url-for tab))))]
                  [:li { :id (if (or (:is-active tab) (if (and tab-url location-url) (. tab-url endsWith location-url))) "active") } 
                    [:a { :href (or tab-url "#") } 
                      (or (:text tab) "Tab") "<span class=\"tab-l\"></span><span class=\"tab-r\"></span>"]]))
              tabs))]
  
        [:hr { :class "noscreen" }]])))