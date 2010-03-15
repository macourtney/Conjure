(ns controllers.home-controller
  (:use conjure.controller.base
        helpers.home-helper))

(defcontroller)

(defaction index [request-map]
  (render-view (home-request-map request-map)))

(defaction list-records [request-map]
  (redirect-to request-map { :action "index" }))
  
(defaction add [request-map]
  (redirect-to request-map { :action "index" }))
  
(defaction error-404 [request-map]
  (render-view (home-request-map request-map)))