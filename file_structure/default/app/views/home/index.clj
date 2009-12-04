(ns views.home.index
  (:use conjure.view.base)
  (:require [clj-html.core :as html]))

(defview []
  (html/html 
    [:div { :class "article" }
      [:h1 "Welcome to Conjure!"]
      [:p "This file, index.clj, can be found in app/views/home directory of your conjure project."]]))