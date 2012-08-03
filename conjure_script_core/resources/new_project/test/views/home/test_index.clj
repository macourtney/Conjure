(ns views.home.test-index
  (:use clojure.test
        views.home.index)
  (:require [conjure.util.request :as request]))

(deftest test-render-view
  (request/with-service-action "home" "index"
    (is (render-view))))