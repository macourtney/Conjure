(ns views.home.index
  (:use conjure.view.base))

(def-view { :layout-info { :links [{ :text "Index", :url-for { :action "index" } }] } } []
  [:div { :class "article" }
    [:h1 "Welcome to Conjure!"]
    [:p "This file, index.clj, can be found in app/views/home directory of your conjure project."]])