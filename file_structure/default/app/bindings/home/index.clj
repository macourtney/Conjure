(ns bindings.home.index
  (:use conjure.binding.base
        helpers.home-helper))

(defbinding []
  (with-home-request-map
    (render-view)))