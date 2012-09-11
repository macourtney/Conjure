(ns flows.test.template-flow
  (:use clojure.test
        flows.template-flow)
  (:require [conjure.util.request :as request]))

(deftest test-model-name
  (let [test-service "foo"]
    (request/set-request-map { :service test-service }
      (is (= (model-name) test-service)))))