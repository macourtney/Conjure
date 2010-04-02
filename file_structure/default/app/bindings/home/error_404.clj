(ns bindings.home.error-404
  (:use conjure.bind.base
        helpers.home-helper))

(defbinding [request-map]
  (render-view (home-request-map request-map)))