(ns bindings.home.error-404
  (:use conjure.binding.base
        helpers.home-helper))

(defbinding [request-map]
  (render-view (home-request-map request-map)))