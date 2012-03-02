(ns views.test.ajax-test
  (:use conjure.core.view.base))

(def-ajax-view [] 
  [:p "You can change this text in app/views/test/ajax_test.clj"])