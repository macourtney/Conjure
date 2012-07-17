(ns views.home.index
  (:use conjure.core.view.base))

(def-view []
  [:div { :class "article" }
    [:h1 "Welcome to Conjure!"]
    [:p "This file, index.clj, can be found in app/views/home directory of your conjure project."]])