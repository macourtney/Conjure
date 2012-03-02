(ns conjure.bind.test.test-base
  (:use clojure.test
        conjure.bind.base)
  (:require [conjure.util.request :as request]))

(def controller-name "test")
(def action-name "show")

(deftest test-render-view
  (request/with-controller-action controller-name action-name
    (let [view (render-view)]
      (is (not (nil? view)))
      (is (map? view)))))