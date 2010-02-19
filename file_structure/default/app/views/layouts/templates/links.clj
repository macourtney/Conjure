(ns views.layouts.templates.links
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.view.util :as view-util]))

(defn
#^{ :doc "Generates the clj-html structure for a given link map." }
  generate-link [link-map original-request-map]
  (let [location-controller (:controller original-request-map)
        location-action (:action original-request-map)
        link-controller (or (:controller (:url-for link-map)) location-controller)
        link-action (or (:action (:url-for link-map)) location-action)
        link-url (or 
                  (:url link-map) 
                  (if (:url-for link-map) 
                    (view-util/url-for original-request-map (:url-for link-map))))]
    [:li 
      { :id 
        (if 
          (or 
            (:is-active link-map) 
            (and link-controller location-controller link-action location-action
              (= link-controller location-controller) (= link-action location-action)))
          "link-active") }
      (or 
        (:link link-map) 
        [:a 
          (merge 
            { :href (or link-url "#") } 
            (:html-options link-map) )
          (:text link-map)])]))

(defview [title links]
  (html/html
    [:h3 [:span title]]

    [:ul { :id "links" }
      (html/htmli (map #(generate-link % (:layout-info request-map)) links))]
      
    [:hr { :class "noscreen" }]))