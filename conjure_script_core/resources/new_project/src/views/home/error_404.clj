(ns views.home.error-404
  (:use conjure.core.view.base))

(def response-map 
  { :status  404
    :headers { "Content-Type" "text/html" } })

(def-view { :response-map response-map } []
  [:div { :class "article" }
    [:h1 "File Not Found"]
    [:p "The file you requested could not be found. Please check your url and try again."]])