(ns views.layouts.templates.links
  (:use conjure.core.view.base)
  (:require [conjure.core.server.request :as request]
            [clojure.tools.string-utils :as string-utils]
            [conjure.core.view.util :as view-util]))

(defn
#^{ :doc "Returns the link controller from the given link map." }
  link-controller [link-map location-controller]
  (string-utils/str-keyword (or (:controller (:url-for link-map)) location-controller)))

(defn
#^{ :doc "Returns the link action from the given link map." }
  link-action [link-map location-action]
  (string-utils/str-keyword (or (:action (:url-for link-map)) location-action)))
  
(defn
#^{ :doc "Returns the link url from the given link map." }
  link-url [link-map original-request-map]
  (or 
    (:url link-map) 
    (if (:url-for link-map) 
      (view-util/url-for (view-util/merge-url-for-params original-request-map (:url-for link-map))))))

(defn
#^{ :doc "Returns the id for a link or nil if no id should be set for the link." }
  link-id [link-map original-request-map]
  (let [location-controller (:controller original-request-map)
        location-action (:action original-request-map)
        link-controller (link-controller link-map location-controller)
        link-action (link-action link-map location-action)]
    (if 
      (or 
        (:is-active link-map) 
        (and link-controller location-controller link-action location-action
          (= link-controller location-controller) (= link-action location-action)))
      "link-active")))

(defn
#^{ :doc "Generates the clj-html structure for a given link map." }
  generate-link [link-map original-request-map]
  (let [link-url (link-url link-map original-request-map)]
    [:li 
      { :id (link-id link-map original-request-map) }
      (or 
        (:link link-map) 
        [:a 
          (merge 
            { :href (or link-url "#") } 
            (:html-options link-map) )
          (:text link-map)])]))

(def-view [title links]
  (list
    [:h3 [:span title]]

    [:ul { :id "links" }
      (doall (map #(generate-link % (request/layout-info)) links))]
      
    [:hr { :class "noscreen" }]))