(ns controllers.home-controller
  (:use conjure.core.controller.base
        helpers.home-helper))

(def-action index
  (bind))

(def-action list-records
  (redirect-to { :action "index" }))
  
(def-action add
  (redirect-to { :action "index" }))
  
(def-action error-404
  (bind))