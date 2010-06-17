(ns conjure.core.binding.test-base
  (:use clojure.contrib.test-is
        conjure.core.binding.base)
  (:require [conjure.core.model.database :as database]
            [conjure.core.server.request :as request]
            [conjure.core.util.session-utils :as session-utils]))

(def controller-name "test")
(def action-name "show")

(deftest test-render-view
  (request/with-controller-action controller-name action-name
    (let [view (render-view)]
      (is (not (nil? view)))
      (is (map? view)))))