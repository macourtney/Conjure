(ns views.templates.application-links
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.view.util :as view-util]))

(defview [title links]
  (let [location-url (:uri request-map)]
    (html/html
      [:h3 [:span title]]
  
      [:ul { :id "links" }
        (html/htmli
          (map 
            (fn [link-map] 
              (let [link-url (or (:url link-map) (if (:url-for link-map) (view-util/url-for request-map (:url-for link-map))))]
                [:li { :id (if (or (:is-active link-map) (if (and link-url location-url) (. link-url endsWith location-url))) "link-active") }
                  (or 
                    (:link link-map) 
                    [:a 
                      (merge 
                        { :href (or link-url "#") } 
                        (:html-options link-map) )
                      (:text link-map)])])) 
              links))]
        
      [:hr { :class "noscreen" }])))