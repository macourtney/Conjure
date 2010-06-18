(ns bindings.home.error-404
  (:use conjure.core.binding.base
        helpers.home-helper))

(def-binding []
  (with-home-request-map
    (render-view)))