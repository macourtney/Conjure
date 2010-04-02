(ns controllers.home-controller
  (:use conjure.controller.base
        helpers.home-helper))

(defaction index
  (bind request-map))

(defaction list-records
  (redirect-to request-map { :action "index" }))
  
(defaction add
  (redirect-to request-map { :action "index" }))
  
(defaction error-404
  (bind request-map))