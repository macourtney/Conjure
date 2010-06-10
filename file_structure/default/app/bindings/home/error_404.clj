(ns bindings.home.error-404
  (:use conjure.binding.base
        helpers.home-helper))

(defbinding []
  (with-home-request-map
    (render-view)))