(ns views.layouts.templates.links
  (:use conjure.view.base)
  (:require [conjure.util.request :as request]
            [clojure.tools.string-utils :as string-utils]
            [conjure.util.conjure-utils :as conjure-util]))

(defn
#^{ :doc "Returns the link service from the given link map." }
  link-service [link-map location-service]
  (string-utils/str-keyword (or (request/service (:url-for link-map)) location-service)))

(defn
#^{ :doc "Returns the link action from the given link map." }
  link-action [link-map location-action]
  (string-utils/str-keyword (or (request/action (:url-for link-map)) location-action)))
  
(defn
#^{ :doc "Returns the link url from the given link map." }
  link-url [link-map original-request-map]
  (or 
    (:url link-map) 
    (if (:url-for link-map) 
      (conjure-util/url-for (conjure-util/merge-url-for-params original-request-map (:url-for link-map))))))

(defn
#^{ :doc "Returns the id for a link or nil if no id should be set for the link." }
  link-id [link-map original-request-map]
  (let [location-service (request/service original-request-map)
        location-action (request/action original-request-map)
        link-service (link-service link-map location-service)
        link-action (link-action link-map location-action)]
    (if 
      (or 
        (:is-active link-map) 
        (and link-service location-service link-action location-action
          (= link-service location-service) (= link-action location-action)))
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