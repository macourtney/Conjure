(ns bindings.home.index
  (:use conjure.bind.base
        helpers.home-helper))

(def-binding []
  (with-home-request-map
    (render-view)))