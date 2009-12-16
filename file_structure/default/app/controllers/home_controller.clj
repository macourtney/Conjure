(ns controllers.home-controller
  (:use [conjure.controller.base]))

(defn index [request-map]
  (render-view 
    (assoc 
      request-map 
      :layout-info 
      { :links 
        [{ :text "Home", :url-for { :controller "home", :action "index" } }]})))

(defn list-records [request-map]
  (redirect-to request-map { :action "index" }))
  
(defn add [request-map]
  (redirect-to request-map { :action "index" }))