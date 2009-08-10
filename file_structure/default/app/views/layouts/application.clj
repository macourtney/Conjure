(ns views.layouts.application
  (:use conjure.view.base)
  (:require [clj-html.core :as html]))

(defview [body]
  (html/html 
    [:html
      [:body
        body]]))