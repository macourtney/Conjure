(ns bindings.templates.direct
  (:use conjure.binding.base))

(defbinding [request-map & params]
  (apply render-view request-map params))