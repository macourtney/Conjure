(ns views.layouts.templates.tabs
  (:use conjure.view.base)
  (:require [clj-html.core :as html]
            [conjure.controller.util :as controller-util]
            [conjure.util.string-utils :as string-utils]
            [conjure.view.util :as view-util]))

(defn 
#^{ :doc "Returns the url for a tab." }
  tab-url [tab-map original-request-map]
  (or 
    (:url tab-map) 
    (if (:url-for tab-map) 
      (view-util/url-for original-request-map (:url-for tab-map)))))

(defn
#^{ :doc "Returns the id for the tab, or nil if no id is needed." }
  tab-id [tab-map original-request-map]
  (let [location-controller (:controller original-request-map)
        tab-controller (string-utils/str-keyword (or (:controller (:url-for tab-map)) location-controller))]
    (if 
      (or 
        (:is-active tab-map) 
        (and tab-controller location-controller (= tab-controller location-controller)))
      "active")))

(defn
#^{ :doc "Generates the clj-html structure for a given tab map." }
  generate-tab [tab-map original-request-map]
  [:li { :id (tab-id tab-map original-request-map)}
    [:a { :href (or (tab-url tab-map original-request-map) "#") } 
      (or (:text tab-map) "Tab") "<span class=\"tab-l\"></span><span class=\"tab-r\"></span>"]])

(defn
#^{ :doc "Returns a tab map generated from a controller name." }
  controller-tab [controller-name]
  { :text (string-utils/human-title-case controller-name), 
    :url-for { :controller controller-name, :action "index" } })

(defn
#^{ :doc "Returns a sequence of tab maps generated from the controllers." }
  controller-tabs []
  (map controller-tab (controller-util/all-controllers)))

(defn
#^{ :doc "Gets or generates all of the tab maps for use by generate-tab." }
  all-tabs [request-map]
  (or (:tabs (:layout-info request-map)) (controller-tabs)))

(defview []
  (html/html
    [:div { :id "tabs", :class "noprint" }
      [:h3 { :class "noscreen" } "Navigation"]
      [:ul { :class "box" }
        (html/htmli (map #(generate-tab % (:layout-info request-map)) (all-tabs request-map)))]
      [:hr { :class "noscreen" }]]))