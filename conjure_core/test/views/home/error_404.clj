(ns views.home.error-404
  (:use conjure.core.view.base)
  (:require [clj-html.core :as html]))

(def-view []
  { :status  404
    :headers { "Content-Type" "text/html" }
    :body (html/html 
      [:div { :class "article" }
        [:h1 "File Not Found"]
        [:p "The file you requested could not be found. Please check your url and try again."]]) })