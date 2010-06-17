(ns views.test.show
  (:use conjure.core.view.base)
  (:require [clj-html.core :as html]))

(def-view []
  (html/html 
    [:p "You can change this text in app/views/test/show.clj"]))