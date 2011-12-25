(ns conjure.core.binding.test-base
  (:use clojure.test
        conjure.core.binding.base)
  (:require [conjure.core.server.request :as request]))

(def controller-name "test")
(def action-name "show")

(deftest test-render-view
  (request/with-controller-action controller-name action-name
    (let [view (render-view)]
      (is (not (nil? view)))
      (is (map? view)))))