(ns controllers.test-controller
  (:use [conjure.flow.base]))

(def-action show
  (println "In the show controller.")
  "")