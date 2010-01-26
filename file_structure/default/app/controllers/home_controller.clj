(ns controllers.home-controller
  (:use [conjure.controller.base]))

(defn-
#^{ :doc "Creates the home links and adds them to the layout info in request-map." }
  home-request-map [request-map]
  (assoc 
      request-map 
      :layout-info 
      { :links 
        [{ :text "Home", :url-for { :controller "home", :action "index" } }]}))

(defn index [request-map]
  (render-view (home-request-map request-map)))

(defn list-records [request-map]
  (redirect-to request-map { :action "index" }))
  
(defn add [request-map]
  (redirect-to request-map { :action "index" }))
  
(defn error-404 [request-map]
  (render-view (home-request-map request-map)))