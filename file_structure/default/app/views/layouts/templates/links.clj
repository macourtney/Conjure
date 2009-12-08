(ns views.layouts.templates.links
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.view.util :as view-util]))

(defview [title links]
  (let [original-request-map (:layout-info request-map)
        location-controller (:controller original-request-map)
        location-action (:action original-request-map)]
    (html/html
      [:h3 [:span title]]
  
      [:ul { :id "links" }
        (html/htmli
          (map 
            (fn [link-map] 
              (let [link-controller (or (:controller (:url-for link-map)) location-controller)
                    link-action (or (:action (:url-for link-map)) location-controller)
                    link-url (or (:url link-map) (if (:url-for link-map) (view-util/url-for original-request-map (:url-for link-map))))]
                [:li { :id (if (or (:is-active link-map) (if (and link-controller location-controller link-action location-action) (and (= link-controller location-controller) (= link-action location-action)))) "link-active") }
                  (or 
                    (:link link-map) 
                    [:a 
                      (merge 
                        { :href (or link-url "#") } 
                        (:html-options link-map) )
                      (:text link-map)])])) 
              links))]
        
      [:hr { :class "noscreen" }])))