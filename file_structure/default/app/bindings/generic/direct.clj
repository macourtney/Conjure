(ns bindings.generic.direct
  (:use conjure.bind.base))

(defbinding [request-map & params]
  (apply render-view request-map params))