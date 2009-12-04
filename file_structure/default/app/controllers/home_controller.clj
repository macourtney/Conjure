(ns controllers.home-controller
  (:use [conjure.controller.base])
  (:require [models.book :as book]))

(defn index [request-map]
  (render-view request-map))

(defn list-records [request-map]
  (redirect-to request-map { :action "index" }))
  
(defn add [request-map]
  (redirect-to request-map { :action "index" }))