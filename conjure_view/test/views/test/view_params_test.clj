(ns views.test.view-params-test
  (:use conjure.view.base))

(def-ajax-view { :response-map { :status 500 } } [] 
  [:p "You can change this text in app/views/test/def_view_params_test.clj"])