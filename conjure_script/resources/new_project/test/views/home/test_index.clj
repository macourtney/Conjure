(ns views.home.test-index
  (:use clojure.test
        views.home.index)
  (:require [conjure.core.server.request :as request]))

(deftest test-render-view
  (request/with-controller-action "home" "index"
    (is (render-view))))