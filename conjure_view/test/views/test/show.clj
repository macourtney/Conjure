(ns views.test.show
  (:use conjure.core.view.base))

(def-view [] 
  [:p "You can change this text in app/views/test/show.clj"])