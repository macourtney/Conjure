(ns views.layouts.templates.tabs
  (:use conjure.core.view.base)
  (:require [conjure.core.controller.util :as controller-util]
            [conjure.core.server.request :as request]
            [clojure.tools.string-utils :as string-utils]
            [conjure.core.view.util :as view-util]))

(defn 
#^{ :doc "Returns the url for a tab." }
  tab-url [tab-map original-request-map]
  (or 
    (:url tab-map) 
    (when-let [url-for (:url-for tab-map)]
      (view-util/url-for (view-util/merge-url-for-params original-request-map url-for)))))

(defn
#^{ :doc "Returns the id for the tab, or nil if no id is needed." }
  tab-id [tab-map original-request-map]
  (let [location-controller (:controller original-request-map)
        tab-controller (string-utils/str-keyword (or (:controller (:url-for tab-map)) location-controller))]
    (when 
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
  (map controller-tab (filter #(not (= % "template")) (controller-util/all-controllers))))

(defn
#^{ :doc "Gets or generates all of the tab maps for use by generate-tab." }
  all-tabs []
  (or (:tabs (request/layout-info)) (controller-tabs)))

(def-view []
  [:div { :id "tabs", :class "noprint" }
    [:h3 { :class "noscreen" } "Navigation"]
    [:ul { :class "box" }
      (doall (map #(generate-tab % (request/layout-info)) (all-tabs)))]
    [:hr { :class "noscreen" }]])