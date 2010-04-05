(ns bindings.generic.direct
  (:use conjure.binding.base))

(defbinding [request-map & params]
  (apply render-view request-map params))