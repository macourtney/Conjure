(ns bindings.home.index
  (:use conjure.bind.base
        helpers.home-helper))

(defbinding [request-map]
  (render-view (home-request-map request-map)))