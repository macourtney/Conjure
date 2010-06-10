(ns controllers.home-controller
  (:use conjure.controller.base
        helpers.home-helper))

(defaction index
  (bind))

(defaction list-records
  (redirect-to { :action "index" }))
  
(defaction add
  (redirect-to { :action "index" }))
  
(defaction error-404
  (bind))