(ns views.home.test-index
  (:use clojure.contrib.test-is
        views.home.index)
  (:require [conjure.core.server.request :as request]))

(deftest test-render-view
  (request/with-controller-action "home" "index"
    (is (render-view))))