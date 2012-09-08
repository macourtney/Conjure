(ns views.layouts.templates.tabs
  (:use conjure.view.base)
  (:require [conjure.flow.util :as flow-util]
            [conjure.util.request :as request]
            [clojure.tools.string-utils :as string-utils]
            [conjure.util.conjure-utils :as conjure-utils]))

(defn 
#^{ :doc "Returns the url for a tab." }
  tab-url [tab-map original-request-map]
  (or 
    (:url tab-map) 
    (when-let [url-for (:url-for tab-map)]
      (conjure-utils/url-for (conjure-utils/merge-url-for-params original-request-map url-for)))))

(defn
#^{ :doc "Returns the id for the tab, or nil if no id is needed." }
  tab-id [tab-map original-request-map]
  (let [location-service (request/service original-request-map)
        tab-service (string-utils/str-keyword (or (request/service (:url-for tab-map)) location-service))]
    (when
      (or
        (:is-active tab-map)
        (and tab-service location-service (= tab-service location-service)))
      "active")))

(defn
#^{ :doc "Generates the clj-html structure for a given tab map." }
  generate-tab [tab-map original-request-map]
  [:li { :id (tab-id tab-map original-request-map)}
    [:a { :href (or (tab-url tab-map original-request-map) "#") } 
      (or (:text tab-map) "Tab") (keyword "<span class=\"tab-l\"></span><span class=\"tab-r\"></span>")]])

(defn
#^{ :doc "Returns a tab map generated from a service name." }
  service-tab [service-name]
  { :text (string-utils/human-title-case service-name), 
    :url-for { :service service-name, :action "index" } })

(defn
#^{ :doc "Returns a sequence of tab maps generated from the services." }
  service-tabs []
  (map service-tab (filter #(not (= % "template")) (flow-util/all-services))))

(defn
#^{ :doc "Gets or generates all of the tab maps for use by generate-tab." }
  all-tabs []
  (or (:tabs (request/layout-info)) (service-tabs)))

(def-view []
  [:div { :id "tabs", :class "noprint" }
    [:h3 { :class "noscreen" } "Navigation"]
    [:ul { :class "box" }
      (doall (map #(generate-tab % (request/layout-info)) (all-tabs)))]
    [:hr { :class "noscreen" }]])