(ns controllers.home-controller
  (:use conjure.controller.base
        helpers.home-helper))

(defn index [request-map]
  (render-view (home-request-map request-map)))

(defn list-records [request-map]
  (redirect-to request-map { :action "index" }))
  
(defn add [request-map]
  (redirect-to request-map { :action "index" }))
  
(defn error-404 [request-map]
  (render-view (home-request-map request-map)))