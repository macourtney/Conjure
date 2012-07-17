(ns bindings.home.index
  (:use conjure.core.binding.base
        helpers.home-helper))

(def-binding []
  (with-home-request-map
    (render-view)))